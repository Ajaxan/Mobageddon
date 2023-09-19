package com.redfootdev.mobageddon.powers

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import com.redfootdev.mobageddon.Mobageddon
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BreakerTest {

    private lateinit var server: ServerMock
    private lateinit var plugin: Mobageddon

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(Mobageddon::class.java)
        // Wacky MockBukkit permissions go brr
        server.pluginManager.addPermission(Permission("cjm.default", PermissionDefault.TRUE))
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }

    @Test
    fun onPlayerJoin_FirstJoin_SendsFirstJoinMessage() {
        server.addPlayer()

        messageTypeMock.assertHasResult()
        assertTrue(messageTypeMock.result.chosenPath.contains(".First-Join.", true))
    }
}