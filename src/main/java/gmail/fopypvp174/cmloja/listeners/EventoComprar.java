package gmail.fopypvp174.cmloja.listeners;

import gmail.fopypvp174.cmloja.Main;
import gmail.fopypvp174.cmloja.enums.LojaEnum;
import gmail.fopypvp174.cmloja.events.LojaBuyOtherPlayer;
import gmail.fopypvp174.cmloja.events.LojaBuyServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EventoComprar implements Listener {

    private Main plugin = Main.getPlugin(Main.class);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onComprar(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player p = e.getPlayer();
        if (e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
            Sign sign = (Sign) e.getClickedBlock().getState();
            Block block = e.getClickedBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
            if (plugin.getUtilidades().isLoja(sign.getLines())) {
                if (sign.getLine(0).equals(p.getDisplayName())) {
                    p.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_erro3", 0, null, null));
                    e.setCancelled(true);
                    return;
                }
                ItemStack item = plugin.getUtilidades().getItemLoja(sign.getLines());
                if (plugin.getUtilidades().getPrices(LojaEnum.COMPRAR, sign) != 0) {
                    if (plugin.getEcon().getBalance(p.getName()) >= plugin.getUtilidades().getPrices(LojaEnum.COMPRAR, sign)) {
                        double slots = Math.floor(Short.parseShort(sign.getLine(1)) / 64);
                        double slotsEmptyPlayer = -1;
                        for (int i = 0; i < p.getInventory().getSize(); i++) {
                            if (p.getInventory().getItem(i) == null) {
                                slotsEmptyPlayer += 1;
                            }
                        }
                        if (slotsEmptyPlayer >= slots) {
                            //Verificar se a placa está num baú
                            if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
                                Chest chest = (Chest) block.getState();
                                removerItensBau(sign, chest, item, p);
                            } else {
                                p.getInventory().addItem(item);
                                buyForce(p, sign, item);
                            }
                        } else {
                            p.sendMessage(plugin.getMessageConfig().message("mensagens.inventory_full", 0, null, null));
                            e.setCancelled(true);
                            return;
                        }
                    } else {
                        p.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_erro1", 0, null, null));
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    //mensagem nao pode comprar
                    p.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_erro4", 0, null, null));
                }
            }
        }
    }

    public void buyForce(Player p, Sign sign, ItemStack itemStack) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(sign.getLine(0));
        double valorFinalVenda = plugin.getUtilidades().getPrices(LojaEnum.COMPRAR, sign);
        int qntPlaca = Integer.parseInt(sign.getLine(1));
        if (sign.getLine(0).equals("[Loja]")) {
            //Adiciona desconto na compra
            for (int i = 0; i <= 100; i++) {
                if (p.hasPermission("*") || p.isOp()) {
                    break;
                }
                if (p.hasPermission("loja.comprar." + i)) {
                    valorFinalVenda -= (valorFinalVenda * i) / 100;
                    p.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_vip_vantagem", i, null, null));
                    break;
                }
            }
            plugin.getUtilidades().removeMoneyVault(p, valorFinalVenda);
            p.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_success", qntPlaca, String.valueOf(valorFinalVenda), null));
            LojaBuyServer eventBuy = new LojaBuyServer(p, valorFinalVenda, itemStack, qntPlaca);
            Bukkit.getServer().getPluginManager().callEvent(eventBuy);
        } else if (target != null) {
            plugin.getUtilidades().giveMoneyVault(target, valorFinalVenda);
            plugin.getUtilidades().removeMoneyVault(p, valorFinalVenda);
            p.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_success", qntPlaca, String.valueOf(valorFinalVenda), null));
            LojaBuyOtherPlayer eventBuy = new LojaBuyOtherPlayer(target, p, valorFinalVenda, itemStack, qntPlaca);
            Bukkit.getServer().getPluginManager().callEvent(eventBuy);
        } else {
            p.sendMessage(plugin.getMessageConfig().message("mensagens.player_unknown", 0, null, target));
        }
    }

    public void removerItensBau(Sign placa, Chest chest, ItemStack item, Player player) {
        short size = (short) chest.getInventory().getSize();
        short slot = 0;
        short quantiaBau = 0;
        short quantiaPlaca = Short.parseShort(plugin.getUtilidades().replace(placa.getLine(1)));
        short quantiaDeu = Short.parseShort(plugin.getUtilidades().replace(placa.getLine(1)));
        List<Short> valores = new ArrayList<>();
        while (slot < size) {
            if (quantiaBau >= quantiaPlaca) {
                break;
            }
            ItemStack itemBau = chest.getInventory().getItem(slot);
            if (itemBau != null && item.isSimilar(itemBau)) {
                quantiaBau += itemBau.getAmount();
                valores.add(slot);
            }
            slot++;
        }
        if (quantiaBau >= quantiaPlaca) {
            ItemStack itemDar = chest.getInventory().getItem(valores.get(0)).clone();
            for (short i = 0; i < valores.size(); i++) {
                ItemStack item2 = chest.getInventory().getItem(valores.get(i));
                short quantiaDoItem = (short) item2.getAmount();
                if (quantiaDeu == 0) {
                    break;
                }
                if (quantiaDoItem == quantiaDeu) {
                    chest.getInventory().clear(valores.get(i));
                    break;
                }
                if (quantiaDoItem > quantiaDeu) {
                    item2.setAmount(quantiaDoItem - quantiaDeu);
                    break;
                }
                if (quantiaDoItem < quantiaDeu) {
                    chest.getInventory().clear(valores.get(i));
                    quantiaDeu -= quantiaDoItem;
                }
            }
            itemDar.setAmount(quantiaPlaca);
            player.getInventory().addItem(itemDar);
            buyForce(player, placa, item);
        } else {
            player.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_erro2", 0, null, null));
        }
    }
}
