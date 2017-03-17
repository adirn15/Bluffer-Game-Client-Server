package protocol;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import server_tpc.ProtocolCallback;


/**
 * @PARAM callback_player : a hash map of the player class of each client
 * @PARAM callback_room : a hash map of the room of each callback (client)
 * @PARAM room_list : a list of all rooms that exist in the game
 * @PARAM string_to_game : a hash map of all the names of games
 */
public class Database {
	
	private ConcurrentHashMap<ProtocolCallback<String>,Player> callback_player; //player->NICK
	private ConcurrentHashMap<ProtocolCallback<String>,Room> callback_room; //player->ROOM
	private LinkedBlockingQueue<Room> room_list;
	private ConcurrentHashMap<String, Class<? extends Game>> string_to_game; 
	
	
	private static class SingleData{
		private static Database instance = new Database();
	}
	
	public static Database getInstance(){
		return SingleData.instance;
	}
	
	private Database(){
		
		callback_player = new ConcurrentHashMap<ProtocolCallback<String>,Player>();
		room_list = new LinkedBlockingQueue<Room>();
		callback_room = new ConcurrentHashMap<ProtocolCallback<String>,Room>();
		string_to_game = new ConcurrentHashMap<String,Class<? extends Game>>();
		string_to_game.put("BLUFFER",Bluffer.class);
	}

	public ConcurrentHashMap<ProtocolCallback<String>, Player> getCallback_player() {
		return callback_player;
	}

	public ConcurrentHashMap<ProtocolCallback<String>, Room> getCallback_room() {
		return callback_room;
	}

	public LinkedBlockingQueue<Room> getRoom_list() {
		return room_list;
	}

	public ConcurrentHashMap<String, Class<? extends Game>> getString_to_game() {
		return string_to_game;
	}
	
	
	
}



