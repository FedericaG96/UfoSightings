package it.polito.tdp.ufo.model;

import java.time.Year;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import it.polito.tdp.ufo.db.SightingsDAO;

public class Model {
	
	//RICORSIONE
	//1 Struttura dati finale
	private List<String> ottima;   //è una lista di stati (string) in cui c'è uno stato di partenza
									// e un insieme di altri stati (non ripetuti)
	
	//2 Struttura dati parziale, cioè una lista definita nel metodo ricorsivo
	
	//3 Condizione di terminazione
	// Dopo un nodo non ci sono più successori che non ho considerato
	
	//4 Generare una nuova soluzione a partire da una soluzione parziale
	// Dato l'ultimo nodo in parziale, considero tutti i successori di quel nodo
	// che non ho ancora considerato
	
	//5 Filtro
	// alla fine ritornerò una sola soluzione --> quella per cui la size() è massima
	
	//6 Livello di ricorsione
	// lunghezza del percorso parziale
	
	//7 Il caso iniziale 
	// parziale contiene il mio stato di partenza

	private SightingsDAO dao ;
	private List<String> stati; //I vertici sono delle semplici stringhe, quindi non serve una mappa
	private Graph<String, DefaultEdge> grafo;
	
	public Model() {
		this.dao= new SightingsDAO();
		this.stati = new LinkedList<String>();
	}
	public List<AnnoCount> getAnni(){
		return dao.getAnni();
	}
	
	public void creaGrafo(Year anno) {
		this.grafo = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		this.stati = this.dao.getStati(anno);
		Graphs.addAllVertices(this.grafo,this.stati);
		
		//soluzione semplice 1: doppio ciclo for, controllo se c'è un arco
		for(String s1: this.grafo.vertexSet()) {
			for(String s2 : this.grafo.vertexSet()) {
				if(!s1.equals(s2)) {
					if(this.dao.esisteArco(s1,s2, anno)) {
						this.grafo.addEdge(s1,s2);
					}
				}
			}
		}
		System.out.println("Grafo creato! ");
		System.out.println("Numero vertici:  "+this.grafo.vertexSet().size());
		System.out.println("Numero archi: "+this.grafo.edgeSet().size());
		

	}
	public int getNvertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int getNarchi() {
		return this.grafo.edgeSet().size();
	}
	public List<String> getStati() {
		return this.stati;
	}
	
	//metodo per ottenere i successori di un nodo
	public List<String> getSuccessori(String stato){
		return Graphs.successorListOf(this.grafo, stato);
	}
	
	//metodo per ottenere i predecessori di un nodo
	public List<String> getPredecessori(String stato){
		return Graphs.predecessorListOf(this.grafo, stato);
	}
	
	public Set<String> getRaggiungibili(String stato){
		
		List<String> raggiungibili = new LinkedList<>();
		DepthFirstIterator<String, DefaultEdge> dp = new DepthFirstIterator<String, DefaultEdge>(this.grafo, stato);
		//con DepthFirstIterator trovo cammini di raggiungibilità
		
		dp.next(); //fa avanzare l'iteratore in modo che nei raggiungibili non ci sia lo stato iniziale
		while(dp.hasNext()) {
			raggiungibili.add(dp.next());
		}
		
		//oppure 
		
		ConnectivityInspector<String, DefaultEdge> cn = new ConnectivityInspector<String, DefaultEdge>(this.grafo);
		Set<String> raggiungibiliSet = cn.connectedSetOf(stato); //Returns a set of all vertices that are in the maximally
																//connected component together with the specified vertex.
		
		return raggiungibiliSet;
	}
	
	public List<String> getPercorsoMassimo(String partenza){
		this.ottima = new LinkedList<String>();		// elenco di stati che fanno parte del percorso di lunghezza massima
		LinkedList<String> parziale = new LinkedList<String>();
		
		parziale.add(partenza); //stato iniziale
		cercaPercorso(parziale);
		return this.ottima;
	}
	
	private void cercaPercorso(LinkedList<String> parziale) {
		
		//ottengo tutti i candidati
		// passo come parametro del metodo getSuccessori, l'ultimo elemento della lista parziale
		List<String> candidati = this.getSuccessori(parziale.get(parziale.size()-1)); 
		for (String candidato : candidati) {
			if(!parziale.contains(candidato)) {	//condizione di terminazione
				// se è un candidato che non ho ancora considerato
				parziale.add(candidato);
				this.cercaPercorso(parziale);
				parziale.remove(parziale.size()-1); //backtracking
			}
		}
		// Vedo se la soluzione corrente è migliore della ottima corrente
		if(parziale.size()> ottima.size()) {
			this.ottima = new LinkedList(parziale);	//faccio un clone
		}
	}
}
