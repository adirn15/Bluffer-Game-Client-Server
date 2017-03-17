package protocol;

import java.util.concurrent.atomic.AtomicInteger;

import server_tpc.ProtocolCallback;

public class Player {
	private ProtocolCallback<String> callback;
	private String nick, response;
	private Room room;
	private AtomicInteger score, guess, oldscore;
	private boolean correct, selected;
	
	public Player(ProtocolCallback<String> c,String n){
		score= new AtomicInteger(0);
		room=null;
		callback=c;
		nick=n;
		response=null;
		guess= new AtomicInteger(-1);
		oldscore= new AtomicInteger(0);
		correct=false;
		selected=true;
	}

	/**
	 * The Method checks if the Player selected response, return true if already selected, else return false
	 * @return
	 */
	public boolean isSelected() {
		return selected;
	}

	public void setSelect(boolean b){
		selected=b;
	}
	
	/**
	 * The function retrun true if the Player answer correct 
	 */
	public boolean isCorrect() {
		return correct;
	}

	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	public int getOldscore() {
		return oldscore.intValue();
	}

	public void setOldscore(int oldscore) {
		this.oldscore.set(oldscore);
	}

	public ProtocolCallback<String> getCallback() {
		return callback;
	}

	public void setCallback(ProtocolCallback<String> callback) {
		this.callback = callback;
	}

	public String getNick() {
		return nick;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public int getScore() {
		return score.intValue();
	}

	public void setScore(int score) {
		this.score.set(score);
	}
	
	public String getResponse(){
		return response;
	}
	
	public void setResponse(String s){
		response = s;
	}

	public void setGuess(int i) {
		guess.set(i);
	}
	
}