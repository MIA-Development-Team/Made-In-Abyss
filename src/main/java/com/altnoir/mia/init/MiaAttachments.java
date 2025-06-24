package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.attachments.Curse;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class MiaAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MIA.MOD_ID);

    public static final Supplier<AttachmentType<Curse>> CURSE =
            ATTACHMENT_TYPES.register("curse", () ->
                    AttachmentType.serializable(Curse::new).build()
            );

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
