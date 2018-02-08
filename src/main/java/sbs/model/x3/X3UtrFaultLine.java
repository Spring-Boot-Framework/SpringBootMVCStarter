package sbs.model.x3;

import java.sql.Timestamp;

public class X3UtrFaultLine {

	private String faultNumber;
	private String utrWorkerCode;
	private X3UtrWorker worker;
	private Timestamp startDateTime;
	private Timestamp endDateTime;
	private int state;
	
	public X3UtrFaultLine() {
		
	}

	public String getFaultNumber() {
		return faultNumber;
	}

	public void setFaultNumber(String faultNumber) {
		this.faultNumber = faultNumber;
	}

	public String getUtrWorkerCode() {
		return utrWorkerCode;
	}

	public void setUtrWorkerCode(String utrWorkerCode) {
		this.utrWorkerCode = utrWorkerCode;
	}

	public Timestamp getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Timestamp startDateTime) {
		this.startDateTime = startDateTime;
	}

	public Timestamp getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(Timestamp endDateTime) {
		this.endDateTime = endDateTime;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "X3UtrFaultLine [faultNumber=" + faultNumber + ", utrWorkerCode=" + utrWorkerCode + ", startDateTime="
				+ startDateTime + ", endDateTime=" + endDateTime + ", state=" + state + "]";
	}

	public X3UtrWorker getWorker() {
		return worker;
	}

	public void setWorker(X3UtrWorker worker) {
		this.worker = worker;
	}

	
	
	
}
