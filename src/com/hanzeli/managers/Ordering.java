package com.hanzeli.managers;

import java.util.Comparator;

import com.hanzeli.values.Order;

/**
 * Class for ordering mechanisms
 * @author Michal
 *
 */
public class Ordering implements Comparator<FileInfo>{

	/** ordering type */
	private Order type;
	/** ordering asc/desc */
	private Order order;
	
	public Ordering(Order type, Order order) {
		this.type = type;
		this.order = order;
	}
	
	public void setType(Order type){
		this.type=type;
	}
	
	public Order getType(){
		return type;
	}
	
	public void setOrder(Order order){
		this.order=order;
	}
	
	public Order getOrder(){
		return order;
	}
	
	private int flip(int i){
		if (order == Order.ASC) return i;
		else return -i;
	}
	public int compare(FileInfo lhs, FileInfo rhs) {

		//folders are always first
		if (!lhs.isFolder() && rhs.isFolder()) {
			return 1;
		}
		if (lhs.isFolder() && !rhs.isFolder()) {
			return -1;
		}
		
		//ordering by name
		if (type == Order.NAME){
			return flip(lhs.getName().compareToIgnoreCase(rhs.getName()));
		}
		//ordering by type
		if (type == Order.FILES) {
			int lhsOrd1 = lhs.getType().getOrdNum();
			int rhsOrd2 = rhs.getType().getOrdNum();
			if (lhsOrd1 != rhsOrd2) {
				return flip((lhsOrd1 < rhsOrd2) ? -1 : 1);
			}
		}

		// Order by time
		if (type == Order.TIME) {
			if (lhs.getLastModif() < rhs.getLastModif()) {
				return flip(-1);
			}
			if (lhs.getLastModif() > rhs.getLastModif()) {
				return flip(1);
			}
		}

		//ordering by size
		if (type == Order.SIZE) {
			if (lhs.getSize() < rhs.getSize()) {
				return flip(-1);
			}
			if (lhs.getSize() > rhs.getSize()) {
				return flip(1);
			}
		}
		//skontrolovat prepadavanie
		//ASC/DESC spravit to ze sa otoci znamienko
		return 0;
	}

}
