package com.github.wujichen158.ikakuji;

import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.command.parser.ForgeAnnotationCommandParser;
import com.envyful.api.forge.gui.factory.ForgeGuiFactory;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.gui.factory.GuiFactory;
import com.github.wujichen158.ikakuji.command.IkaKujiCmd;
import com.github.wujichen158.ikakuji.command.completion.CrateDeliverCompleter;
import com.github.wujichen158.ikakuji.command.completion.CrateNameCompleter;
import com.github.wujichen158.ikakuji.config.IkaKujiCfg;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.listener.KujiTriggerListener;
import com.github.wujichen158.ikakuji.listener.PlayerIOListener;
import com.github.wujichen158.ikakuji.util.CfgPostProcessUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(Reference.MOD_ID)
public class IkaKuji {

    private static IkaKuji INSTANCE;

    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);

    private final ForgePlayerManager playerManager = new ForgePlayerManager();
    private final ForgeCommandFactory commandFactory = new ForgeCommandFactory(ForgeAnnotationCommandParser::new, this.playerManager);
    private IkaKujiCfg config;
    private IkaKujiLocaleCfg locale;

    public IkaKuji() {
        INSTANCE = this;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static IkaKuji getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new IkaKuji();
        }
        return INSTANCE;
    }

    public void loadConfig() {
        createDirsIfNotExist();
        try {
            this.config = YamlConfigFactory.getInstance(IkaKujiCfg.class);
            this.locale = YamlConfigFactory.getInstance(IkaKujiLocaleCfg.class);

            CfgPostProcessUtil.loadAllCrates();
            CfgPostProcessUtil.cleanUpCurrentKuji();

        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }

    private void createDirsIfNotExist() {
        try {
            Path teamPath = Paths.get(Reference.CRATE_PATH);
            if (Files.notExists(teamPath)) {
                Files.createDirectories(teamPath);
            }

            Path dataPath = Paths.get(Reference.DATA_PATH);
            if (Files.notExists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }

    @SubscribeEvent
    public void preInit(final FMLServerAboutToStartEvent event) {
        GuiFactory.setPlatformFactory(new ForgeGuiFactory());

        loadConfig();
    }

    @SubscribeEvent
    public void init(final FMLServerStartingEvent event) {
        MinecraftForge.EVENT_BUS.register(new KujiTriggerListener());
        MinecraftForge.EVENT_BUS.register(new PlayerIOListener());
    }

    @SubscribeEvent
    public void closing(FMLServerStoppingEvent event) {
    }

    @SubscribeEvent
    public void onCommandRegistration(RegisterCommandsEvent event) {
        this.commandFactory.registerCompleter(new CrateDeliverCompleter());
        this.commandFactory.registerCompleter(new CrateNameCompleter());
        this.commandFactory.registerCommand(event.getDispatcher(), this.commandFactory.parseCommand(new IkaKujiCmd()));
    }

    public ForgePlayerManager getPlayerManager() {
        return playerManager;
    }

    public ForgeCommandFactory getCommandFactory() {
        return commandFactory;
    }

    public IkaKujiCfg getConfig() {
        return config;
    }

    public IkaKujiLocaleCfg getLocale() {
        return locale;
    }
}
