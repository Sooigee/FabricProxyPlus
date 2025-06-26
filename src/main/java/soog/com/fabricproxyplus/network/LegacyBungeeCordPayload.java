package soog.com.fabricproxyplus.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LegacyBungeeCordPayload(byte[] data) implements CustomPayload {
    public static final CustomPayload.Id<LegacyBungeeCordPayload> ID = new CustomPayload.Id<>(
        Identifier.of("minecraft", BungeeCordProtocol.LEGACY_CHANNEL.toLowerCase())
    );
    public static final PacketCodec<PacketByteBuf, LegacyBungeeCordPayload> CODEC = CustomPayload.codecOf(LegacyBungeeCordPayload::write, LegacyBungeeCordPayload::new);
    
    private LegacyBungeeCordPayload(PacketByteBuf buf) {
        this(buf.readByteArray());
    }
    
    private void write(PacketByteBuf buf) {
        buf.writeByteArray(data);
    }
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}