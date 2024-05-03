package com.github.wujichen158.ikakuji.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.github.wujichen158.ikakuji.lib.Reference;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigPath(Reference.CONFIG_PATH)
@ConfigSerializable
public class IkaKujiCfg extends AbstractYamlConfig {
}
