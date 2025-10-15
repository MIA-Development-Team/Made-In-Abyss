package com.altnoir.mia.gametest;

import com.altnoir.mia.MIA;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;


@GameTestHolder(MIA.MOD_ID)
public class MiaGameTests {
    @PrefixGameTestTemplate(false)
    @GameTest(template = "test_template")
    public static void exampleTest(GameTestHelper helper) {
        helper.succeedWhen(() -> {

        });
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(MiaGameTests.class);
    }
}
