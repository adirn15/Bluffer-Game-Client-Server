package protocol;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import both_servers.AsyncServerProtocol;
import both_servers.ServerProtocol;
import server_tpc.ProtocolCallback;

public class TBGProtocol implements AsyncServerProtocol<String> {

	/**
	 * @PARAM callback_player : a hash map of the player class of each client
	 * @PARAM callback_room : a hash map of the room of each callback (client)
	 * @PARAM room_list : a list of all rooms that exist in the game
	 * @PARAM string_to_game : a hash map of all the names of games
	 * @PARAM shouldClose : is the client in termination process
	 * @PARAM connectionTerminated : is the connection terminated
	 */
	private ConcurrentHashMap<ProtocolCallback<String>,Player> callback_player; //player->NICK
	private ConcurrentHashMap<ProtocolCallback<String>,Room> callback_room; //player->ROOM
	private LinkedBlockingQueue<Room> room_list;
	private ConcurrentHashMap<String,Class<? extends Game>> string_to_game;
	private boolean shouldClose = false;
	private boolean connectionTerminated = false;
	
	public TBGProtocol(){
		Database d = Database.getInstance();
		callback_player=d.getCallback_player();
		callback_room=d.getCallback_room();
		room_list=d.getRoom_list();
		string_to_game=d.getString_to_game();
	}

	/**
	 * if the client sent "QUIT" and is not during a game all his info will be deleted from the server
	 * @PARAM msg : the msg from the user (command backspace data)
	 * @param callback : the callback of the user
	 * @return if the connection with the client should end or not
	 */
    public boolean isEnd(String msg, ProtocolCallback<String> callback)
    {	if (msg.equals("QUIT")){
    		
    		Room player_room;
    		if (callback_player.containsKey(callback)){ //user has a nick
	        	Player cur_player = callback_player.get(callback);
    			if (callback_room.containsKey(callback)){ // user has a room
    				player_room = callback_room.get(callback);
    				if (!player_room.isActive()){
    					if (player_room.removeUser(cur_player)){
    		        		room_list.remove(player_room);
		        		}
		        		else{ // remove the room from the rooms list
		        			room_list.remove(player_room);
		        		}
    					callback_room.remove(callback); //remove user from room map
	        		}
	        		else{
	        			try {
							callback.sendMessage("SYSMSG QUIT REJECTED: game is in session");
						} catch (IOException e) {
							e.printStackTrace();
						}
	        			return false;
	        		}
    			}
	    		callback_player.remove(callback); //remove user from players list
    		}
    		try {
				callback.sendMessage("SYSMSG QUIT ACCEPTED");
			} catch (IOException e) {
				e.printStackTrace();
			}
    		return true;	
    	}
    	else
    		return false;
    }

    /**
     * depending on the command sent in the msg the data after the command will be sent to the appropriate function case"command".
     * if the command is unidentified the user will be notified
     * @PARAM msg : the msg from the user (command backspace data)
	 * @param callback : the callback of the user
	 * 
     */
	public void processMessage(String msg, ProtocolCallback<String> callback) {

		String command = "";
		int i=0;
		if (msg.contains(" ")){
			while (msg.charAt(i)!=' '){
				command=command+msg.charAt(i);
				i++;
			}
			String data=msg.substring(i+1);
			if ((data.contains(" ") && !command.equals("MSG") )|| data.length()==0){
				try {
					callback.sendMessage("SYSMSG "+command+" REJECTED : bad input, only single words accepted");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			else{
				String response="";
				try {
					if (command.equals("NICK")){
						response = caseNick(data,callback);
						callback.sendMessage(response);		
					}
					else if (!hasNick(callback)){	// ALL OTHER COMMANDS REQUIRE A NICK
						response = "SYSMSG "+command+" REJECTED : register before sending any commands";
						callback.sendMessage(response);		
					}
					else if (command.equals("JOIN")){
						response = caseJoin(data,callback);
						callback.sendMessage(response);
					}
					else if (command.equals("MSG")){
						caseMsg(data,callback); 
					}
					else if (command.equals("STARTGAME")){
						caseStartGame(data,callback);
					}
					else if (command.equals("TXTRESP")){
						caseTxtResp(data,callback);
					}
					else if (command.equals("SELECTRESP")){
						caseSelectResp(data,callback);
					}
					else
						callback.sendMessage("SYSMSG "+command+" UNIDENTIFIED");
					
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else{	// commands without " "
			if (msg.equals("LISTGAMES")){
				String response = caseListGames(callback);
				try {
					callback.sendMessage(response);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if (msg.equals("QUIT")){
				return;
			} 
			else
				try {
					callback.sendMessage("SYSMSG "+command+" UNIDENITIFED");
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	//********************************* COMMAND CASES *************************************

	
	
	/**
	 *
	 * @param data : the nick requested
	 * @param callback : unique to user
	 * @return the message to be sent back to user according to the result of his request
	 */
	private String caseNick(String data, ProtocolCallback<String> callback){
		if (callback_player.containsKey(callback))
			return "SYSMSG NICK REJECTED : you already have a nick";
		Player p = getPlayerFromNick(data);
		if (p==null){
			callback_player.put(callback,new Player(callback,data));
			return "SYSMSG NICK ACCEPTED";
		}
		return "SYSMSG NICK REJECTED : nick already in use";
	}
	
	/**
	 * cases where request will be rejected: no nick, client has an active room, requested room is during an active game.
	 * it is possible to move to another room as long as it is not active (during game session)
	 * if necessary a new room will be created, and if user left an empty room it will be deleted from server 
	 * @param data - room requested
	 * @param callback - unique to user
	 * @return
	 */
	private String caseJoin(String data,ProtocolCallback<String> callback){

		Player user = callback_player.get(callback);
		Room room_to_add_user = getRoomFromName(data);
		
		Room curr_room=null;
		if (callback_room.containsKey(callback)){ //client has a room 
			curr_room = callback_room.get(callback); 
			
			//*** player has active room ****
			if (curr_room.isActive())
				return "SYSMSG JOIN REJECTED : Current room is during  a game"; 
			else{  //*** player has room but is not active ****
				if (room_to_add_user==null){ // *** wanted room doesn't exist ***
					if (curr_room.removeUser(user))//updates room and returns if empty now
						room_list.remove(curr_room);
					room_to_add_user = new Room(user,data);
					
					callback_room.remove(callback); //update call backs map
					callback_room.put(callback,room_to_add_user); 
					
					room_list.add(room_to_add_user);
					user.setRoom(room_to_add_user);
					return "SYSMSG JOIN ACCEPTED : switched to new room - "+data; 
				}
				else{ 	// *** wanted room already exist ***
					if (room_to_add_user.isActive()) //wanted room is active
						return "SYSMSG JOIN REJECTED : wanted room is currently in a game"; 
					else{ // *** wanted room exists and not active ***
						
						if (curr_room.removeUser(user))//updates room and returns if empty now
							room_list.remove(curr_room);
						room_to_add_user.addUser(user);
						
						callback_room.remove(callback); //update call backs map
						callback_room.put(callback,room_to_add_user);
						
						room_list.add(room_to_add_user);
						user.setRoom(room_to_add_user);
						return "SYSMSG JOIN ACCEPTED : switched room to "+data; 
					}	
				}
			}
		
		}
		else{ // player doesn't have a room
			if (room_to_add_user==null){ // *** wanted room doesn't exist ***
				
				room_to_add_user = new Room(user,data);
				callback_room.put(callback,room_to_add_user); 
				room_list.add(room_to_add_user);
				user.setRoom(room_to_add_user);
				return "SYSMSG JOIN ACCEPTED : joined a new room "+data; 
			}
			else{ 	// *** wanted room already exist ***
				if (room_to_add_user.isActive()) //wanted room is active
					return "SYSMSG JOIN REJECTED : wanted room is currently in a game"; 
				else{ // *** wanted room exists and not active ***
					room_to_add_user.addUser(user);
					callback_room.put(callback,room_to_add_user);
					//room_list.add(room_to_add_user);
					user.setRoom(room_to_add_user);
					return "SYSMSG JOIN ACCEPTED : joined room "+data; 
				}	
			}
		}
		
		
	}
	
	/**
	 * reject cases: user has no room
	 * sends the message to all others
	 * @param data - msg to send to other users in the room
	 * @param callback - unique to user
	 */
	private void caseMsg(String data,ProtocolCallback<String> callback){
		if (!callback_room.containsKey(callback)){
			try {
				callback.sendMessage("SYSMSG MSG REJECTED : choose a room first ");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		Room curr_room = callback_room.get(callback);
		for(Player p : curr_room.getClients()){
			try {
				p.getCallback().sendMessage("USRMSG "+callback_player.get(callback).getNick()+" : "+data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * prints the list of available games (currently just BLUFFER)
	 * @param callback - unique to user
	 * @return
	 */
	private String caseListGames(ProtocolCallback<String> callback){
		String response = "SYSMSG LISTGAMES : ";
		for (String s : string_to_game.keySet() ){
			response = response +s+",";
		}
		String resp=response.substring(0,response.length()-1);
		return resp;	
	}
	
	/**
	 * the function creates a new game in the room and sets the room as active
	 * reject cases: game in room already being played, no nickname, no room, game doesn't exist
	 * @param data - name of the game to start
	 * @param callback - unique to user
	 */
	private void caseStartGame(String data,ProtocolCallback<String> callback){
		try{
			if (!callback_room.containsKey(callback))
				callback.sendMessage("SYSMSG STARTGAME REJECTED : join a room first");
			else{
				Room room = callback_room.get(callback);
				if (room.isActive())
					callback.sendMessage("SYSMSG STARTGAME REJECTED : game is already in process in this room");
				else{
					Set<String> list_games = string_to_game.keySet();
					if (!list_games.contains(data)){
						callback.sendMessage("SYSMSG STARTGAME REJECTED : no such game on server");
						return;
					}
					for(Player p : room.getClients())
						p.getCallback().sendMessage("SYSMSG STARTGAME ACCEPTED : game "+data+" starting...");
					
					synchronized(room){
						room.setActive(true);
						GameCreator creator = new GameCreator();
						creator.createGame(room,data);
					}
				}
			}	
		}
		catch(IOException e){
			e.printStackTrace();
		}	
	}
	
	/**
	 * reject cases: no game is running, or not the time to send this type of response in the game
	 * @param data - the made up answer by the user
	 * @param callback - unique to user
	 */
	private void caseTxtResp(String data,ProtocolCallback<String> callback){
		Player p = callback_player.get(callback);
		if (!hasGame(callback)){
			try {
				callback.sendMessage("SMSG TXTRESP FAILED : no game is running");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		boolean success= callback_room.get(callback).getGame().canAddResponse(p);
		try{
			if (success){
				callback.sendMessage("SYSMSG TEXTRESP ACCEPTED");
				callback_room.get(callback).getGame().addResponse(data, p);
			}
			else
				callback.sendMessage("SMSG TXTRESP FAILED : cannot send responses at the moment");
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * reject cases: no game, not time to select response, illegal number format,  to accept: 0<=data<=num of players in the room
	 * @param data - integer answer to select
	 * @param callback - unique to user
	 */
	private void caseSelectResp(String data,ProtocolCallback<String> callback){
		try{
			if (!hasGame(callback)){
				callback.sendMessage("SYSMSG SELECTRESP REJECTED : no game is running");
				return;
			}
			if (callback_player.get(callback).isSelected()){
				callback.sendMessage("SYSMSG SELECTRESP REJECTED : you can't select a response right now");
				return;
			}
			Game g = callback_room.get(callback).getGame();
			int guess;
			try{
				guess = Integer.parseInt(data);
				if (guess<0 || guess>callback_room.get(callback).getNum_of_players()){
					callback.sendMessage("SYSMSG SELECTRESP REJECTED : illegal number");
					return;
				}
			}
			catch (NumberFormatException e){
				callback.sendMessage("SYSMSG SELECTRESP REJECTED : illegal number format");
				return;
			}
			int num_of_choices = g.getNumPlayers()+1;
			if (guess<=num_of_choices && guess>=0){
				callback.sendMessage("SYSMSG SELECTRESP ACCEPTED");
				boolean game_over = g.selectResp(guess,callback_player.get(callback));
				if (game_over){
					callback_room.get(callback).setActive(false);
					callback_room.get(callback).setGame(null);
					
				}
			}
			else{
				callback.sendMessage("SYSMSG SELECTRESP REJECTED : illegal value, choose a number between 0 to "+num_of_choices);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}		
	}

	
	//******************************* PRIVATE FUNCTIONS ***********************************
	
	/**
	 * runs over callback to room has map and checks if any callback has a match
	 * @param roomname 
	 * @return the Room @param roomname if already exists on server or null if it doesn't
	 */
	private Room getRoomFromName(String roomname){ //return room by name, if doesn't exist returns null
		Room r=null;
		for (ProtocolCallback<String> c : callback_room.keySet()){
			r=callback_room.get(c);
			if (r.getRoom_name().equals(roomname))
				return r;
		}
		return null;
	}

	/**
	 * 
	 * @param nick - nickname reqested
	 * @return the Player with name nick if exists or null otherwise
	 */
	private Player getPlayerFromNick(String nick){ //return player by nick, if doesn't exist returns null
		Player p=null;
		for (ProtocolCallback<String> c : callback_player.keySet()){
			p=callback_player.get(c);
			if (p.getNick().equals(nick))
				return p;
		}
		return null;
	}

	public boolean hasRoom(ProtocolCallback<String> c){
		return (callback_room.containsKey(c));
	}
	
	public boolean hasNick(ProtocolCallback<String> c){
		return (callback_player.containsKey(c));
	}
	
	public boolean hasGame(ProtocolCallback<String> c){
		if (!hasRoom(c))
			return false;

		return (callback_room.get(c).getGame()!=null);
	}

	/**
	 * Is the protocol in a closing state?.
	 * When a protocol is in a closing state, it's handler should write out all pending data, 
	 * and close the connection.
	 * @return true if the protocol is in closing state.
	 */
	public boolean shouldClose() {
		return this.shouldClose;
	}

	/**
	 * Indicate to the protocol that the client disconnected.
	 */
	public void connectionTerminated() {
		this.connectionTerminated = true;
	}


}