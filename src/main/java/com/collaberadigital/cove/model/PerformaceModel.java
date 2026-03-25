package com.collaberadigital.cove.model;

public class PerformaceModel {

	 	private boolean admin;
	 	private boolean superAdmin;
	 	private String status;
	 	private Data data;
		public boolean isAdmin() {
			return admin;
		}
		public void setAdmin(boolean admin) {
			this.admin = admin;
		}
		public boolean isSuperAdmin() {
			return superAdmin;
		}
		public void setSuperAdmin(boolean superAdmin) {
			this.superAdmin = superAdmin;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public Data getData() {
			return data;
		}
		public void setData(Data data) {
			this.data = data;
		}
	 	
	 	
	 	
	
}
