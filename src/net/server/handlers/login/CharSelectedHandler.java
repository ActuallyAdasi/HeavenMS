/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.server.handlers.login;

import client.MapleClient;
import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.extern.log4j.Log4j2;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.coordinator.session.MapleSessionCoordinator;
import net.server.coordinator.session.MapleSessionCoordinator.AntiMulticlientResult;
import net.server.world.World;
import org.apache.mina.core.session.IoSession;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

@Log4j2
public final class CharSelectedHandler extends AbstractMaplePacketHandler {
    
    private static int parseAntiMulticlientError(AntiMulticlientResult res) {
        log.warn("Parsing anti multiclient error. res: {}", res);
        switch (res) {
            case REMOTE_PROCESSING:
                return 10;

            case REMOTE_LOGGEDIN:
                return 7;

            case REMOTE_NO_MATCH:
                return 17;
                
            case COORDINATOR_ERROR:
                return 8;
                
            default:
                return 9;
        }
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int charId = slea.readInt();

        String macs = slea.readMapleAsciiString();
        String hwid = slea.readMapleAsciiString();

        if (!hwid.matches("[0-9A-F]{12}_[0-9A-F]{8}")) {
            log.warn("CharSelectedHandler got hwid that isn't of an expected format! Announcing after login error!");
            c.announce(MaplePacketCreator.getAfterLoginError(17));
            return;
        }

        c.updateMacs(macs);
        c.updateHWID(hwid);

        IoSession session = c.getSession();
        AntiMulticlientResult res = MapleSessionCoordinator.getInstance().attemptGameSession(session, c.getAccID(), hwid);
        if (res != AntiMulticlientResult.SUCCESS) {
            log.warn("attemptGameSession did not return AntiMulticlientResult.SUCCESS! Announcing after login error!");
            c.announce(MaplePacketCreator.getAfterLoginError(parseAntiMulticlientError(res)));
            return;
        }

        if (c.hasBannedMac() || c.hasBannedHWID()) {
            log.warn("Client with banned MAC or banned Hardware ID detected! Closing session immediately.");
            MapleSessionCoordinator.getInstance().closeSession(session, true);
            return;
        }

        Server server = Server.getInstance();
        if(!server.haveCharacterEntry(c.getAccID(), charId)) {
            log.warn("Character already has character entry detected! Closing session immediately.");
            MapleSessionCoordinator.getInstance().closeSession(session, true);
            return;
        }

        c.setWorld(server.getCharacterWorld(charId));
        World wserv = c.getWorldServer();
        if(wserv == null || wserv.isWorldCapacityFull()) {
            log.warn("Server is null or is at capacity! Announcing after login error!");
            c.announce(MaplePacketCreator.getAfterLoginError(10));
            return;
        }

        String[] socket = server.getInetSocket(c.getWorld(), c.getChannel());
        if(socket == null) {
            log.warn("Server socket is null! Announcing after login error!");
            c.announce(MaplePacketCreator.getAfterLoginError(10));
            return;
        }

        server.unregisterLoginState(c);
        c.setCharacterOnSessionTransitionState(charId);

        try {
            c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (final UnknownHostException | NumberFormatException e) {
            log.error("UnknownHostException | NumberFormatException caught in CharSelectedHandler!", e);
        }
        log.info("CharSelectedHandler executed successfully.");
    }
}
