package protocol;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class is present a room in the network that can run a game inside.
 *
 */
public class Room {
	private boolean isActive=false;
	private AtomicInteger num_of_players;
	private LinkedBlockingQueue<Player> clients;
	private Game game;
	private String room_name;
	
	public Room(Player p, String rm_name){
		num_of_players = new AtomicInteger(1);
		clients = new LinkedBlockingQueue<Player>();
		clients.add(p);
		game=null;
		room_name=rm_name;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public int getNum_of_players() {
		return num_of_players.intValue();
	}

	public void setNum_of_players(int num_of_players) {
		this.num_of_players.set(num_of_players);
	}

	public LinkedBlockingQueue<Player> getClients() {
		return clients;
	}

	public void setClients(LinkedBlockingQueue<Player> clients) {
		this.clients = clients;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public String getRoom_name() {
		return room_name;
	}

	public void setRoom_name(String room_name) {
		this.room_name = room_name;
	}
	/**
	 * The method is remove the Player (p- param) from the room
	 */
	public boolean removeUser(Player p){
		clients.remove(p);
		num_of_players.decrementAndGet();
		if (num_of_players.intValue()==0)
			return true;
		return false;
	}
	
	/**
	 * The method is add the Player (p- param) to the room
	 */
	public void addUser(Player p){
		clients.add(p);
		num_of_players.incrementAndGet();
	}
	
}