package protocol;

public class GameCreator{
	
	/**
	 * The method create and running a new Game(s) in the room(r)
	 * @param r is the room that the game is going to start
	 * @param s is the name of the game
	 */
	public void createGame(Room r,String s){

		if (s.equals("BLUFFER")){
			r.setGame(new Bluffer(r));
		}
	}
}