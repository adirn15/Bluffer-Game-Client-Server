package protocol;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

import protocol.Json_questions.question;

public class Bluffer extends Game{
	
	public Bluffer(Room r){
		super(r);
		start();
	}

	public boolean canAddResponse(Player p){
		if ((!player_response.containsKey(p)) && player_response.size()<num_of_players.intValue())
			return true;
		return false;	
	}
	

	public void addResponse(String resp, Player p){
		synchronized(player_response){
			player_response.put(p, resp);
			p.setResponse(resp);
			if (player_response.size()==num_of_players.intValue()){ //CAN SEND THE QUESTION
				String ask_choices="ASKCHOICES : ";
				int i=0;
				boolean added=false;
				for (Player player : player_response.keySet()){ //add responses to the choices
					ask_choices = ask_choices+i+".";
					if (i==real_ans_index.intValue()){		//insert the real answer
						ask_choices=ask_choices+realAnswer+" "+(i+1)+".";
						responses_array[i]=realAnswer;
						i++;
						added=true;
					}
					ask_choices= ask_choices+player_response.get(player)+" ";
					responses_array[i]=player_response.get(player);
					i++;
				}
				if (!added)
					ask_choices=ask_choices+" "+i+"."+realAnswer;
				// send to all players
				for (Player player : room.getClients()){
					try {
						p.setSelect(false);
						player.getCallback().sendMessage(ask_choices);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}	
		}
	}
	
	/**
	 * uploads the json files and sends to the room the asktxt command
	 */
	public void start(){
		int random = (int)(Math.random()*num_of_players.intValue());
		real_ans_index.set(random);
		gson=new Gson();
		try {
			reader = new BufferedReader(new FileReader("Bluffer.json"));
			questions_data =gson.fromJson(reader,Json_questions.class);
			question q= questions_data.questions[round.intValue()-1];
			questionText=q.questionText;
			realAnswer = q.realAnswer;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		for (Player p : room.getClients()){
			try {
				p.setScore(0);
				p.setOldscore(0);
				p.getCallback().sendMessage("ASKTXT : "+questionText);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param guess is an answer (number) that Player p is select
	 * @param p is the Player that guess the answer
	 * @return return true if the game is end, else true
	 */
	public boolean selectResp(int guess, Player p) {
		
		p.setSelect(true);
		p.setOldscore(p.getScore());
		if (guess == real_ans_index.intValue()){ // if chose the right answer
			p.setCorrect(true);
			p.setScore(p.getScore()+10);	
		}
		else{ //wrong answer
			String chosen_ans=responses_array[guess];		
			System.out.println(chosen_ans);
			for (Player player : room.getClients()){ //find the player who created this response and give him +5 points
				if (player.getResponse().equals(chosen_ans)){
					player.setScore(player.getScore()+5);
					break;
				}
			}
		}
		if (num_of_answers.incrementAndGet()==num_of_players.intValue()){ //everyone answered- finish the round
			return endRound();
		}
		else return false;
	}
	
	/**
	 * The method finished the round and prepared the game for another one(if the game isnt end)
	 * @return return true if the game is end, else true
	 */
	private boolean endRound(){
		try {
			for (Player p : room.getClients()){
				int points_to_add = (p.getScore()-p.getOldscore());
				p.getCallback().sendMessage("GAMEMSG the correct answer is : "+responses_array[real_ans_index.intValue()] );
				if (p.isCorrect())
					p.getCallback().sendMessage("GAMEMSG correct! +"+points_to_add+" points");
				else
					p.getCallback().sendMessage("GAMEMSG wrong! +"+points_to_add+" points");	
			
				p.setResponse(null);
				p.setGuess(-1);
				p.setCorrect(false);
			}			
		}	
		catch(IOException e){
			e.printStackTrace();
		}
		responses_array=new String[num_of_players.intValue()+1];
		player_response.clear();
		realAnswer="";
		num_of_answers.set(0);
		int random = (int)(Math.random()*num_of_players.intValue());
		real_ans_index.set(random);
		round.incrementAndGet();
		if (round.intValue()==4)
			return endGame();
		else{
			gson=new Gson();
			try {
				reader = new BufferedReader(new FileReader("Bluffer.json"));
				questions_data =gson.fromJson(reader,Json_questions.class);
				question q= questions_data.questions[round.intValue()-1];
				questionText=q.questionText;
				realAnswer = q.realAnswer;
				for (Player p : room.getClients()){
					p.getCallback().sendMessage("ASKTXT : "+questionText);	
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			return false;
	}
	
	/**
	 * The method ending the game
	 * @return return true if the game is successfully ended
	 */
	private boolean endGame(){
		String summary="GAMEMSG Summary: ";
		for (Player p : room.getClients()){
			summary=summary+p.getNick()+": "+p.getScore()+" points, ";
		}
		summary= summary.substring(0,summary.length()-2); //delete the last comma
		for (Player p : room.getClients()){
			try{
				p.getCallback().sendMessage(summary);
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		return true;
	}
}