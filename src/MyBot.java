

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/*
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
*/
/*
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
*/



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyBot extends TelegramLongPollingBot {
	
    @Override
    public void onUpdateReceived(Update update) {
    	
        
    	String datos = obtenerJsonBrawl("U2VYCYV");
    	String datos_format = parsearDatos(datos);


        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            SendMessage message = new SendMessage() // Create a message object object
                    .setChatId(chat_id)
                    //.setText(message_text);
                    .setText(datos_format);
            try {
                execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    	
    }
    
    public String parsearDatos(String datos) {
    	String[] cut = datos.split(",");
    	String toReturn = cut[2] + "\n" + cut[3];    	
    	for(int i=6; i<19; i++) 
    		toReturn = toReturn + "\n" + cut[i];
    	toReturn = toReturn.replaceAll("\":", ": ");
    	toReturn = toReturn.replaceAll("\"", "");
    	

		// Pongo el nick y el tag juntos en 1 sola linea
    	String nick = obtenerDato(toReturn, "name");
    	String tag = obtenerDato(toReturn, "tag");
    	// Borro linea de tag para ponerla junto con el nombre
    	toReturn = toReturn.replaceAll("tag: "+tag+"\n", "");
		toReturn = toReturn.replaceAll("name: (.*?)\n", "Nick: "+nick+" (#"+tag+")\n");
		
		
		// Pongo trofeos y trofeos_max juntos en 1 sola linea
		String trofeos = obtenerDato(toReturn, "trophies");
		String trofeos_max = obtenerDato(toReturn, "highestTrophies");
    	// Borro linea de trofeos-max para ponerla junto con los trofeos
    	toReturn = toReturn.replaceAll("highestTrophies: "+trofeos_max+"\n", "");		
		toReturn = toReturn.replaceAll("trophies: "+trofeos+"\n", "Trofeos: "+trofeos+" (máx: "+trofeos_max+")\n");
		

		// corto lucha estelar para ponerlo mas abajo
		String lucha_estelar = obtenerDato(toReturn, "highestPowerPlayPoints");
    	toReturn = toReturn.replaceAll("highestPowerPlayPoints: "+lucha_estelar+"\n", "");		
		
		
		toReturn = toReturn.replaceAll("expLevel", "Nivel");
		toReturn = toReturn.replaceAll("totalExp: (.*?)\n", "");
		toReturn = toReturn.replaceAll("expFmt", "Experiencia");
		toReturn = toReturn.replaceAll("victories", "Victorias");
		toReturn = toReturn.replaceAll("soloShowdownVictories", "Victorias Solo");
		toReturn = toReturn.replaceAll("duoShowdownVictories", "Victorias Duo");
		toReturn = toReturn.replaceAll("bestRoboRumbleTimeInSeconds: (.*?)\n", "Max puntos Lucha estelar: "+lucha_estelar+"\n");
		toReturn = toReturn.replaceAll("bestRoboRumbleTime", "Tiempo Pelea Robótica");
		toReturn = toReturn.replaceAll("bestTimeAsBigBrawlerInSeconds: (.*?)\n", "");
		toReturn = toReturn.replaceAll("bestTimeAsBigBrawler", "Tiempo Megabrawler");
		
		// Obtengo cantidad de brawlers desbloqueados
		String brawlers_desbloq = cut[295];
		brawlers_desbloq = brawlers_desbloq.replaceAll("\":", ": ");
		brawlers_desbloq = brawlers_desbloq.replaceAll("\"", "");
		brawlers_desbloq = obtenerDato(brawlers_desbloq+"\n", "brawlersUnlocked");
		toReturn = toReturn + "\nBrawlers desbloqueados: " + brawlers_desbloq;
    	
		
    	return toReturn;
    }
    
    // retorna valor de X campo mirando en todos los datos
    public String obtenerDato(String datos, String campo) {

    	// busco el campo
    	Pattern patron = Pattern.compile(campo+": (.*?)\n");
		Matcher buscador = patron.matcher(datos);
		
		buscador.find();
		String toReturn = buscador.group();
    	
		// selecciono el valor
    	String[] cut = toReturn.split(":");
    	toReturn = cut[1];
    	toReturn = toReturn.replaceAll(" ", "");
    	toReturn = toReturn.replaceAll("\n", "");
    	return toReturn;
    }
    
  
    
    public String obtenerJsonBrawl(String tag) {  
    	String toReturn = "NULL";      	
    	try {
    	  // Sending get request
    	  String urlString = "https://api.starlist.pro/v1/player?tag="+tag;
    	  
    	  URL url = new URL(urlString);
    	  HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	 
    	  // By default it is GET request
    	  //con.setRequestMethod("GET");
    	 
    	  //add request header
    	  con.setRequestProperty("Authorization", this.getApiBrawlToken());
    	 
    	  int responseCode = con.getResponseCode();
    	  System.out.println("Sending get request : "+ url);
    	  System.out.println("Response code : "+ responseCode);
    	 
    	  // Reading response from input Stream
    	  BufferedReader in = new BufferedReader(
    	          new InputStreamReader(con.getInputStream()));
    	  String output;
    	  StringBuffer response = new StringBuffer();
    	 
    	  while ((output = in.readLine()) != null) 
    		  response.append(output);
    	  
    	  in.close();
    	 
    	  //printing result from response
    	  //System.out.println(response.toString());
    	  
    	  toReturn = response.toString();
    	 
    	 
    	} catch(Exception e1) {
    		System.out.println("Error: API BRAWL - Metodo 'obtenerJsonBrawl(String tag)'");
    	}    	
    	return toReturn;
    }
    

    @Override
    public String getBotUsername() {
        return "WecherBot";
    }

    @Override
    public String getBotToken() {
        return "381001945:AAGyWyIaze8GVbX5UDQopEhC-5EDZHKXXRs";
    }
    
    public String getApiBrawlToken() {
    	return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaXNjb3JkX3VzZXJfaWQiOiI0MTkxNTg1NTE0NjY5OTk4MjEiLCJyZWFzb24iOiJwcm9iYW5kby10b2tlbi1icmF3bCIsInZlcnNpb24iOjEsImlhdCI6MTU4Mzc4MDQ2OH0.wxAkKDOyGmSWGKCVeqayq-CBs_BfZMzh-qAs18tHHFU";
    }
}
