package protocol;
import java.io.BufferedReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

public abstract class Game {
	
	protected Gson gson;
	protected BufferedReader reader;
	protected Json_questions questions_data;
	protected AtomicInteger round,  num_of_answers,  num_of_players,  real_ans_index;
	protected ConcurrentHashMap<Player,String> player_response;
	protected String[] responses_array;
	protected String questionText, realAnswer;
	protected Room room;

	public Game(Room r){
		this.room=r;
		num_of_answers= new AtomicInteger(0);
		num_of_players= new AtomicInteger(r.getClients().size());
		round= new AtomicInteger(1);
		real_ans_index = new AtomicInteger(0);
		player_response = new ConcurrentHashMap<Player,String>();
		responses_array = new String[num_of_players.intValue()+1];
	}
	
	/**
	 * The method check if specifies player can add a response
	 * @param p, the Player that want to add response
	 * @return true- if the player can add a response, else false
	 */
	public abstract boolean canAddResponse(Player p);		
	
	/**
	 * the Method add the response resp of player p
	 * @param resp is a String that player p want to add
	 * @param p is the player that want to add a response
	 */
	public abstract void addResponse(String resp, Player p);	
	
	/**
	 * The method is running the game
	 */
	public abstract void start();
	
	public abstract boolean selectResp(int guess, Player p);
	
	public void setRoom(Room r)
	{
		this.room=r;
	}
	public void setNumOfPlayers(int n)
	{
		this.num_of_players.set(n);
	}
	
	public int getNumPlayers(){
		return num_of_players.intValue();
	}
	
	Game create_game(String game, Room room) throws NullPointerException{
		synchronized (room){
			if (game=="BLUFFER")
				return new Bluffer(room);
			else
				throw new NullPointerException("no such game exists on server");
		}
	}
}
