package model;


import annotation.Autowired;

public class Boss {

	private Office office;
	private Car car;

	@Autowired
	public Boss(Office office, Car car) {
		this.office = office;
		this.car = car;
	}

	public String toString() {
		return "This boss: Office -> " + office + ", car -> " + car + ". ";
	}

}
