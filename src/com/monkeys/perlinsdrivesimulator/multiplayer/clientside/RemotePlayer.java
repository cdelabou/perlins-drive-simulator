package com.monkeys.perlinsdrivesimulator.multiplayer.clientside;

import java.util.Date;

import com.monkeys.perlinsdrivesimulator.Main;
import com.monkeys.perlinsdrivesimulator.scene.game.Player;

public class RemotePlayer extends Player {
	protected int id;
	protected long lastUpdateTime, lastPingTime, currentTime;
	protected boolean usernameRequested = false;
	private RemoteConnection connection;
	
	public RemotePlayer(RemoteConnection connection, int id) {
		// La classe sup�rieure ne n�cessite pas processing, on lui �vite donc cet ennui
		// TODO: mettre � jour si la classe Player le requiert un jour
		super(null);
		
		this.lastUpdateTime = new Date().getTime() + 5000; // on laisse 10s avant le premier ping
		this.username = "unnamed";
		this.connection = connection;
		this.id = id;
	}
	
	// La m�thode ne sert plus qu'a v�rifier la diff�rence de temps depuis la derni�re actualisation
	public void update(Main p) {
		currentTime = new Date().getTime();
		
		// Si le joueur n'est pas mis � jour depuis 5 secondes
		if (currentTime > lastUpdateTime + 5000) {
			
			// Si un ping est envoy�, on attend 5 secondes avant le prochain
			if (currentTime > lastPingTime + 5000) {
				// On envoie une demande de ping
				connection.send(RequestType.PING, "" + id);
			
				lastPingTime = currentTime;
			}
		}
		
		// Si on n'a jamais demand� le nom d'utilisateur, il est surement temps
		if (!usernameRequested) {
			connection.send(RequestType.WHO_IS, "" + id);
			usernameRequested = true;
		}
		
		// On met quand m�me la mise � jour de la position en fonction de la vitesse (en cas
		// de coupure ca risque d'�tre dr�le)
		this.position.add(this.speed);
		this.angle += rotationSpeed;
	}
	
	// G�re la requ�te recue du serveur
	public RemotePlayer request(int reqType, String data) {
		if (reqType == RequestType.PONG_DEAD.id) {
			
			// On se supprime simplement de la liste si le joueur associ� est d�connect�
			connection.getPlayers().remove(id);
			
		} else if (reqType == RequestType.POSITION.id) {
			// Si c'est une MaJ de position, on�content
			String parts[] = data.split(" ");
			
			this.position.x = Float.parseFloat(parts[0]);
			this.position.y = Float.parseFloat(parts[1]);
			this.speed.x = Float.parseFloat(parts[2]);
			this.speed.y = Float.parseFloat(parts[3]);
			this.angle = Float.parseFloat(parts[4]);
			this.rotationSpeed = Float.parseFloat(parts[5]);
		
		} else if (reqType == RequestType.I_AM.id) {
			username = data;
		}
		
		// On met � jour le temps de la derni�re mise � jour
		lastUpdateTime = currentTime;
		
		return this;
	}
}
