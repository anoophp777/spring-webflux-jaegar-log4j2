package anoop.h.webflux;

/**
 * @author Anoop Hallimala 2018
 */
public class Restaurant {
	
	public Restaurant() {
		// TODO Auto-generated constructor stub
	}
	public Restaurant(String name, double pricePerPerson) {
		this.name = name;
		this.pricePerPerson = pricePerPerson;
	}

	private String name;
	private double pricePerPerson;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPricePerPerson() {
		return pricePerPerson;
	}
	public void setPricePerPerson(double pricePerPerson) {
		this.pricePerPerson = pricePerPerson;
	}
}
