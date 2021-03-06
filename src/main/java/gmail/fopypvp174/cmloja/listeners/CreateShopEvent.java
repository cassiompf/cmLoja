package gmail.fopypvp174.cmloja.listeners;

import gmail.fopypvp174.cmloja.api.Utilidades;
import gmail.fopypvp174.cmloja.configurations.LojaConfig;
import gmail.fopypvp174.cmloja.configurations.MessageConfig;
import gmail.fopypvp174.cmloja.exceptions.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class CreateShopEvent implements Listener {

    private final Economy economy;
    private final MessageConfig messageConfig;
    private final LojaConfig lojaConfig;
    private final Plugin plugin;

    public CreateShopEvent(Economy economy, MessageConfig messageConfig, LojaConfig lojaConfig, Plugin plugin) {
        this.economy = economy;
        this.messageConfig = messageConfig;
        this.lojaConfig = lojaConfig;
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void onCriar(SignChangeEvent e) {
        Player player = e.getPlayer();

        Sign sign = (Sign) e.getBlock().getState();

        if (!Utilidades.isLojaValid(e.getLines())) {
            return;
        }

        try {
            createSignLoja(player, e.getLines(), sign);
            e.setLine(2, Utilidades.updatePriceSign(e.getLine(2)));
            player.sendMessage(messageConfig.message("mensagens.criar_success"));
        } catch (CreateSignNickOtherPlayerException error) {
            e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
            e.getBlock().setType(Material.AIR);
            player.sendMessage(messageConfig.message("mensagens.criar_erro3"));
            return;
        } catch (CreateSignPlayerWithoutPermissionException error) {
            e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
            player.sendMessage(messageConfig.message("mensagens.criar_erro5"));
            return;
        } catch (CreateSignServerWithoutPermissionException error) {
            e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
            player.sendMessage(messageConfig.message("mensagens.criar_erro4"));
            return;
        } catch (CreateSignItemInvalidException error) {
            e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
            player.sendMessage(messageConfig.message("mensagens.criar_erro2"));
            return;
        } catch (CreateSignWithoutChestException error) {
            e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
            player.sendMessage(messageConfig.message("mensagens.criar_erro1"));
            return;
        } catch (CreateSignServerOnChestException error) {
            e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
            player.sendMessage(messageConfig.message("mensagens.criar_erro6"));
            return;
        }
    }

    private void createSignLoja(Player player, String[] lines, org.bukkit.block.Sign sign)
            throws CreateSignPlayerWithoutPermissionException, CreateSignWithoutChestException, CreateSignItemInvalidException, CreateSignNickOtherPlayerException, CreateSignServerWithoutPermissionException, CreateSignServerOnChestException {

        if ((!player.hasPermission("loja.admin")) && (!player.hasPermission("loja.player")) && (!player.isOp())) {
            throw new CreateSignPlayerWithoutPermissionException("O player " + player.getName() + " tentou criar loja sem permissão.");
        }
        if (Utilidades.getItemLoja(lines, lojaConfig) == null) {
            throw new CreateSignItemInvalidException("O player " + player.getName() + " tentou criar uma loja com um item inválido: " + lines[3]);
        }
        Block block = sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
        String placaLoja = messageConfig.getCustomConfig().getString("placa.nomeLoja");
        if (!Utilidades.replaceShopName(lines[0]).equals(placaLoja)) {
            if ((!block.getType().equals(Material.CHEST)) && (!block.getType().equals(Material.TRAPPED_CHEST))) {
                throw new CreateSignWithoutChestException("O player " + player.getName() + " tentou criar uma loja fora do baú.");
            }
            if (!Utilidades.replaceShopName(lines[0]).equals(player.getName())) {
                throw new CreateSignNickOtherPlayerException("O player " + player.getName() + " tentou criar uma loja com o nick de outro player.");
            }
            return;
        }
        if ((block.getType().equals(Material.CHEST)) || (block.getType().equals(Material.TRAPPED_CHEST))) {
            throw new CreateSignServerOnChestException("O player " + player.getName() + " tentou criar uma loja do servidor em um baú.");
        }
        if ((!player.hasPermission("loja.admin")) && (!player.isOp())) {
            throw new CreateSignServerWithoutPermissionException("O player " + player.getName() + " tentou criar uma loja com o nome do servidor sem permissão.");
        }
    }
}
