package be.raffon.arena.inventories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Page {

    public HashMap<Integer, CItem> items;
    public Integer rows;
    public String title;
    public Integer page;
    public Boolean end;
    public boolean closable = true;

    public Page(Integer rows, String title, Integer page, Boolean end, Boolean closable) {
        this.items = new HashMap<Integer, CItem>();
        this.rows = rows;
        this.title = title;
        this.page = page;
        this.end= end;
        this.closable = closable;
    }

    public Page(Integer rows, String title, Integer page, Boolean end) {
        this.items = new HashMap<Integer, CItem>();
        this.rows = rows;
        this.title = title;
        this.page = page;
        this.end= end;
    }


    public void addItem(CItem is, int slot) {
        if(items.get(slot) != null) {
            items.remove(slot);
        }

        items.put(slot, is);
    }

    public Inventory returnInv() {

        Inventory inv = Bukkit.createInventory(null, 9*rows+9, title);


        ItemStack br = new ItemStack(Material.BARRIER, 1);
        ItemMeta brm = br.getItemMeta();
        brm.setDisplayName(ChatColor.BLUE + "Exit");
        br.setItemMeta(brm);

        ItemStack pagafter = new ItemStack(Material.ARROW, 1);
        ItemMeta pagem = pagafter.getItemMeta();
        pagem.setDisplayName(ChatColor.GREEN + "Page " + String.valueOf(page+1));
        pagafter.setItemMeta(pagem);

        ItemStack pagprev = new ItemStack(Material.ARROW, 1);
        ItemMeta pagpem = pagprev.getItemMeta();
        pagpem.setDisplayName(ChatColor.GREEN + "Page " + String.valueOf(page-1));
        pagprev.setItemMeta(pagpem);

        ItemStack pane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
        ItemMeta panem = pane.getItemMeta();
        panem.setDisplayName(" ");
        pane.setItemMeta(panem);

        items.put(9*rows, new CItem(pane, "{nothing}"));
        items.put(9*rows+1, new CItem(pane, "{nothing}"));
        items.put(9*rows+2, new CItem(pane, "{nothing}"));
        items.put(9*rows+3, new CItem(pane, "{nothing}"));
        if(closable) {
            items.put(9*rows+4, new CItem(br, "{leave}"));
        }
        items.put(9*rows+5, new CItem(pane, "{nothing}"));
        items.put(9*rows+6, new CItem(pane, "{nothing}"));
        items.put(9*rows+7, new CItem(pane, "{nothing}"));
        items.put(9*rows+8, new CItem(pane, "{nothing}"));

        if(page-1 >= 1) {
            items.put(9*rows, new CItem(pagprev, "{previouspage}"));
        }
        if(!end) {
            items.put(9*rows, new CItem(pagafter, "{nextpage}"));
        }

        for (Map.Entry<Integer, CItem> entry : items.entrySet()) {

            inv.setItem(entry.getKey(), entry.getValue().itemstack);

        }



        return inv;
    }

    public CItem getItem(Integer slot) {
        return items.get(slot);
    }


}
