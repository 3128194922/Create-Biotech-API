package com.nobodiiiii.createbiotech.network;

import com.nobodiiiii.createbiotech.CreateBiotech;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class CBPackets {

	private static final String NETWORK_VERSION = "1";
	private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(CreateBiotech.asResource("main"))
		.serverAcceptedVersions(NETWORK_VERSION::equals)
		.clientAcceptedVersions(NETWORK_VERSION::equals)
		.networkProtocolVersion(() -> NETWORK_VERSION)
		.simpleChannel();

	private CBPackets() {}

	public static void sendToTrackingEntity(Object packet, Entity entity) {
		CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
	}

	public static void sendToPlayer(Object packet, ServerPlayer player) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
	}

	public static void sendToServer(Object packet) {
		CHANNEL.sendToServer(packet);
	}
}
