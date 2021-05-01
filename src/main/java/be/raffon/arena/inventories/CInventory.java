package be.raffon.arena.inventories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CInventory {
	
	public ArrayList<Page> pages;
	public HashMap<String, String> variables;
	public Integer currentpage;
	
	public CInventory() {
		this.pages = new ArrayList<Page>();
		this.currentpage = 1;
	}
	
	public CInventory(HashMap<String, String> variables) {
		this.variables = variables;
		this.pages = new ArrayList<Page>();
		this.currentpage = 1;
	}
	
	
	public void clickItem(ItemStack is, Integer slot, Inventory inv, Player p, InventoryClickEvent evt, InventoryManager im, Boolean right) {
		for(int i=0; i<pages.size(); i++) {
			Page page = pages.get(i);
			Inventory ine = page.returnInv();
			if(Arrays.equals(inv.getContents(), ine.getContents())) {
				CItem it = page.getItem(slot);
				it.execute(evt, im);
			}
		}
	}
	
	public void changePage(Integer page, Player pl) {
		this.currentpage = page;
		pl.openInventory(pages.get(page).returnInv());
	}
	
	public void addPage(Page pag) {
		pages.add(pag);
	}
	
	public void display(Player p) {
		if(pages.size() < 1) {
			p.sendMessage("The data is empty !");
			return;
		}
		p.openInventory(pages.get(currentpage-1).returnInv());
	}

}
