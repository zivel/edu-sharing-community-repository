package org.edu_sharing.repository.client.rpc.cache;

import java.io.Serializable;

public class CacheInfo implements Serializable{
	
	int size;
	
	

	
	long statisticHits;
	
	
	String name;
	
	int backupCount;
	
	long backupEntryCount;
	
	long backupEntryMemoryCost;
	
	long heapCost;
	
	long ownedEntryCount;
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}


	public long getStatisticHits() {
		return statisticHits;
	}

	public void setStatisticHits(long statisticCacheHitCount) {
		this.statisticHits = statisticCacheHitCount;
	}


	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public int getBackupCount() {
		return backupCount;
	}

	public void setBackupCount(int backupCount) {
		this.backupCount = backupCount;
	}

	public long getBackupEntryCount() {
		return backupEntryCount;
	}

	public void setBackupEntryCount(long backupEntryCount) {
		this.backupEntryCount = backupEntryCount;
	}

	public long getBackupEntryMemoryCost() {
		return backupEntryMemoryCost;
	}

	public void setBackupEntryMemoryCost(long backupEntryMemoryCost) {
		this.backupEntryMemoryCost = backupEntryMemoryCost;
	}

	public long getHeapCost() {
		return heapCost;
	}

	public void setHeapCost(long heapCost) {
		this.heapCost = heapCost;
	}
	
	public void setOwnedEntryCount(long ownedEntryCount) {
		this.ownedEntryCount = ownedEntryCount;
	}
	
	public long getOwnedEntryCount() {
		return ownedEntryCount;
	}
	
	

}
