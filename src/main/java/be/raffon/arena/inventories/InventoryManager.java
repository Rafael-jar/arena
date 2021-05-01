package be.raffon.arena.inventories;

import java.util.HashMap;
import java.util.UUID;

public class InventoryManager {
	
	public HashMap<UUID, CInventory> invs;
	
	public InventoryManager() {
		this.invs = new HashMap<UUID, CInventory>();
	}
	
	public CInventory getInventory(UUID uuid) {
		return invs.get(uuid);
	}
	
	public void registerInventory(UUID uuid, CInventory cinv) {
		invs.put(uuid, cinv);
	}
	
	public void unregisterInventory(UUID uuid) {
		invs.remove(uuid);
	}

}
