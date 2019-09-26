package dev.cooltools.docx.processor;

import java.util.Arrays;
import java.util.List;

public class Person {
	final private String civilite;
	final private String nom;
	final private String prenom;
	final private String ville;

	final private List<Person> copains;

	public Person(String civilite,String nom,String prenom, Person...copains) {
		this(civilite, nom, prenom, null, copains);
	}
	
	public Person(String civilite,String nom,String prenom, String ville, Person...copains) {
		this.civilite = civilite;
		this.nom = nom;
		this.prenom = prenom;
		this.ville = ville;
		this.copains = Arrays.asList(copains);
	}

	public String getCivilite() {
		return civilite;
	}

	public String getNom() {
		return nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public List<Person> getCopains() {
		return copains;
	}

	public String getVille() {
		return ville;
	}
}