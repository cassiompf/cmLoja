package gmail.fopypvp174.cmloja;

import gmail.fopypvp174.cmloja.api.Utilidades;
import gmail.fopypvp174.cmloja.cmds.GerarItem;
import gmail.fopypvp174.cmloja.config.LojaConfig;
import gmail.fopypvp174.cmloja.config.MessageConfig;
import gmail.fopypvp174.cmloja.listeners.EventoComprar;
import gmail.fopypvp174.cmloja.listeners.EventoCriar;
import gmail.fopypvp174.cmloja.listeners.EventoPlayer;
import gmail.fopypvp174.cmloja.listeners.EventoVender;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private LojaConfig loja;
    private MessageConfig messageConfig;
    private Economy econ;
    private Utilidades utilidades;

    @Override
    public void onEnable() {
        if (setupVault() == false) {
            Bukkit.getLogger().info(String.format("[%s] Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
        }
        loja = new LojaConfig(this, "itens.yml", "itens.yml");
        messageConfig = new MessageConfig(this, "configurar.yml", "configurar.yml");
        utilidades = new Utilidades();
        getServer().getPluginManager().registerEvents(new EventoCriar(), this);
        getServer().getPluginManager().registerEvents(new EventoComprar(), this);
        getServer().getPluginManager().registerEvents(new EventoVender(), this);
        getServer().getPluginManager().registerEvents(new EventoPlayer(), this);

        getCommand("geraritem").setExecutor(new GerarItem());
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[cmLoja] Plugin ativado com sucesso!");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[cmLoja] Autor: C4ssi0");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[cmLoja] GitHub: github.com/C4ssi0/cmLoja");
    }

    public boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            econ = rsp.getProvider();
            return econ != null;
        }
        return false;
    }

    @Override
    public void onDisable() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            loja.save();
            messageConfig.save();
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Plugin [cmLoja] desativado com sucesso!");
        }
    }

    public Utilidades getUtilidades() {
        return utilidades;
    }

    public LojaConfig getLoja() {
        return loja;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public Economy getEcon() {
        return econ;
    }
}
