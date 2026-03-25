package com.collaberadigital.cove.model;

public class InnovationData {

	private String domain;
    private int ideate;
    private int concept;
    private int prototype;
    private int testing;
    
    
    
	public InnovationData(String domain, int ideate, int concept, int prototype, int testing) {
		super();
		this.domain = domain;
		this.ideate = ideate;
		this.concept = concept;
		this.prototype = prototype;
		this.testing = testing;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public int getIdeate() {
		return ideate;
	}
	public void setIdeate(int ideate) {
		this.ideate = ideate;
	}
	public int getConcept() {
		return concept;
	}
	public void setConcept(int concept) {
		this.concept = concept;
	}
	public int getPrototype() {
		return prototype;
	}
	public void setPrototype(int prototype) {
		this.prototype = prototype;
	}
	public int getTesting() {
		return testing;
	}
	public void setTesting(int testing) {
		this.testing = testing;
	}
    
    
}
