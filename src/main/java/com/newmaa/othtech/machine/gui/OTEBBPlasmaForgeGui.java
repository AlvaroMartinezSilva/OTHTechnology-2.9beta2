package com.newmaa.othtech.machine.gui;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.newmaa.othtech.machine.OTEBBPlasmaForge;

import gregtech.api.modularui2.GTGuiTextures;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;

/**
 * MUI2 (ModularUI2) GUI for the {@link OTEBBPlasmaForge}, decoupled from the
 * machine — same pattern as GT5U's {@code MTEPlasmaForgeGui}.
 *
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Registers the {@code wireless} sync value (C2S) for the wireless-mode toggle.</li>
 * <li>Adds the wireless-mode button to the right button column — at the same
 * position as DTPF's convergence button ({@code MTEPlasmaForgeGui}), i.e. the
 * top of the column, above the structure-update button. Icon on/off states and
 * the click gating (controller slot) match DTPF exactly.</li>
 * </ul>
 * All machine logic (MLevel, hatches, NBT, rule enforcement) stays in the
 * machine; this class is purely presentational.
 */
public class OTEBBPlasmaForgeGui extends MTEMultiBlockBaseGui<OTEBBPlasmaForge> {

    public OTEBBPlasmaForgeGui(OTEBBPlasmaForge multiblock) {
        super(multiblock);
    }

    @Override
    protected void registerSyncValues(PanelSyncManager syncManager) {
        super.registerSyncValues(syncManager);
        syncManager.syncValue(
            "wireless",
            new BooleanSyncValue(multiblock::isWirelessModeEnabled, multiblock::setWirelessModeEnabled).allowC2S());
    }

    @Override
    protected Flow createButtonColumn(ModularPanel panel, PanelSyncManager syncManager) {
        // 按钮列子项自底向上排列,追加在最上面 = 与 DTPF 收敛按钮相同的位置
        return super.createButtonColumn(panel, syncManager).child(createWirelessModeButton(syncManager));
    }

    /**
     * Wireless-mode toggle button, mirroring {@code MTEPlasmaForgeGui#createConvergenceButton}:
     * <ul>
     * <li>icon: {@code TT_SAFE_VOID_ON/OFF}, driven by the synced value (not a client
     * machine field) so it always reflects the server state;</li>
     * <li>click: gated on the controller slot holding the calibration matrix
     * (a synced inventory slot, always current on the client — unlike the derived
     * MLevel field), then toggles the synced value with {@code allowC2S()}.</li>
     * </ul>
     * The MLevel / energy-hatch rule is enforced server-side in
     * {@link OTEBBPlasmaForge#setWirelessModeEnabled(boolean)}.
     */
    protected IWidget createWirelessModeButton(PanelSyncManager syncManager) {
        BooleanSyncValue wirelessSyncer = syncManager.findSyncHandler("wireless", BooleanSyncValue.class);
        return new ButtonWidget<>().marginBottom(2)
            .tooltip(t -> t.addLine(IKey.lang("ote.bbpf.5")))
            .overlay(new DynamicDrawable(() -> {
                // 与 DTPF 一致:激活 → TT_SAFE_VOID_ON,关闭 → TT_SAFE_VOID_OFF
                if (wirelessSyncer.getBoolValue()) {
                    return GTGuiTextures.TT_SAFE_VOID_ON;
                }
                return GTGuiTextures.TT_SAFE_VOID_OFF;
            }))
            .onMousePressed(mouseButton -> {
                // 亿万火种之怒,燃尽此身!
                if (!multiblock.hasCalibrationMatrix()) return false;
                wirelessSyncer.setBoolValue(!wirelessSyncer.getBoolValue());
                return true;
            });
    }
}
