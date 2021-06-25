package net.crashcraft.crashclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.menus.list.PlayerPermListMenu;
import net.crashcraft.crashclaim.menus.list.SubClaimListMenu;
import net.crashcraft.crashclaim.menus.permissions.SimplePermissionMenu;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ClaimMenu extends GUI {
    private final Claim claim;
    private final PermissionHelper helper;
    private final GUI previousMenu;

    private boolean hasSubClaims;

    public ClaimMenu(Player player, Claim claim, GUI previousMenu) {
        super(player, "Claim Menu", 54);
        this.claim = claim;
        this.previousMenu = previousMenu;
        this.helper = PermissionHelper.getPermissionHelper();
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        hasSubClaims = false;

        ItemStack descItem;

        if (claim.getOwner().equals(getPlayer().getUniqueId())){
            descItem = Localization.MENU__GENERAL__CLAIM_ITEM_NO_OWNER.getItem(
                    "name", claim.getName(),
                    "min_x", Integer.toString(claim.getMinX()),
                    "min_z", Integer.toString(claim.getMinZ()),
                    "max_x", Integer.toString(claim.getMaxX()),
                    "max_z", Integer.toString(claim.getMaxZ()),
                    "world", Bukkit.getWorld(claim.getWorld()).getName()
            );
        } else {
            descItem = Localization.MENU__GENERAL__CLAIM_ITEM.getItem(
                    "name", claim.getName(),
                    "min_x", Integer.toString(claim.getMinX()),
                    "min_z", Integer.toString(claim.getMinZ()),
                    "max_x", Integer.toString(claim.getMaxX()),
                    "max_z", Integer.toString(claim.getMaxZ()),
                    "world", Bukkit.getWorld(claim.getWorld()).getName(),
                    "owner", Bukkit.getOfflinePlayer(claim.getOwner()).getName()
            );
        }

        descItem.setType(GlobalConfig.visual_menu_items.get(claim.getWorld()));
        inv.setItem(13, descItem);

        if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
            inv.setItem(28, Localization.MENU__PERMISSIONS__BUTTONS__PER_PLAYER.getItem());
            inv.setItem(29, Localization.MENU__PERMISSIONS__BUTTONS__GLOBAL.getItem());
        } else {
            inv.setItem(28, Localization.MENU__PERMISSIONS__BUTTONS__PER_PLAYER_DISABLED.getItem());
            inv.setItem(29, Localization.MENU__PERMISSIONS__BUTTONS__GLOBAL_DISABLED.getItem());
        }

        if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
            inv.setItem(32, Localization.MENU__PERMISSIONS__BUTTONS__RENAME.getItem());
            inv.setItem(33, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_ENTRY.getItem());
            inv.setItem(34, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_EXIT.getItem());
            inv.setItem(49, Localization.MENU__PERMISSIONS__BUTTONS__DELETE.getItem());
        } else {
            inv.setItem(32, Localization.MENU__PERMISSIONS__BUTTONS__RENAME_DISABLED.getItem());
            inv.setItem(33, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_ENTRY_DISABLED.getItem());
            inv.setItem(34, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_EXIT_DISABLED.getItem());
            inv.setItem(49, Localization.MENU__PERMISSIONS__BUTTONS__DELETE_DISABLED.getItem());
        }

        hasSubClaims = false;
        for (SubClaim subClaim : claim.getSubClaims()){
            if (helper.hasPermission(subClaim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)
                    || helper.hasPermission(subClaim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                inv.setItem(30, Localization.MENU__PERMISSIONS__BUTTONS_SUBCLAIMS.getItem());
                hasSubClaims = true;
                break;
            }
        }
        if (!hasSubClaims){
            inv.setItem(30, Localization.MENU__PERMISSIONS__BUTTONS_NO_SUBCLAIMS.getItem());
        }

        if (previousMenu != null){
            inv.setItem(45, Localization.MENU__GENERAL__BACK_BUTTON.getItem());
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        switch (event.getSlot()){
            case 28:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                    new PlayerPermListMenu(claim, getPlayer(), this);
                } else {
                    player.sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage());
                    forceClose();
                }
                break;
            case 29:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                    new SimplePermissionMenu(player, claim, null, this).open();
                } else {
                    player.sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage());
                    forceClose();
                }
                break;
            case 30:
                if (hasSubClaims) {
                    new SubClaimListMenu(getPlayer(), this, claim).open();
                }
                break;
            case 32:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new AnvilGUI.Builder()
                            .plugin(CrashClaim.getPlugin())
                            .itemLeft(Localization.MENU__CLAIM__RENAME__MESSAGE.getItem())
                            .onComplete(((player, reply) -> {
                                claim.setName(reply);
                                player.sendMessage(Localization.MENU__CLAIM__RENAME__CONFIRMATION.getMessage("name", reply));
                                return AnvilGUI.Response.close();
                            }))
                            .open(getPlayer());
                } else {
                    player.sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage());
                    forceClose();
                }
                break;
            case 33:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new AnvilGUI.Builder()
                            .plugin(CrashClaim.getPlugin())
                            .itemLeft(Localization.MENU__CLAIM__ENTRY_MESSAGE__MESSAGE.getItem())
                            .onComplete(((player, reply) -> {
                                claim.setEntryMessage(reply);
                                player.sendMessage(Localization.MENU__CLAIM__ENTRY_MESSAGE__CONFIRMATION.getMessage("entry_message", reply));
                                return AnvilGUI.Response.close();
                            }))
                            .open(getPlayer());
                } else {
                    player.sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage());
                    forceClose();
                }
                break;
            case 34:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new AnvilGUI.Builder()
                            .plugin(CrashClaim.getPlugin())
                            .itemLeft(Localization.MENU__CLAIM__EXIT_MESSAGE__MESSAGE.getItem())
                            .onComplete(((player, reply) -> {
                                claim.setExitMessage(reply);
                                player.sendMessage(Localization.MENU__CLAIM__EXIT_MESSAGE__CONFIRMATION.getMessage("exit_message", reply));
                                return AnvilGUI.Response.close();
                            }))
                            .open(getPlayer());
                } else {
                    player.sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage());
                    forceClose();
                }
                break;
            case 49:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    ItemStack message = Localization.UN_CLAIM__MENU__CONFIRMATION__MESSAGE.getItem();
                    message.setType(GlobalConfig.visual_menu_items.get(claim.getWorld()));

                    new ConfirmationMenu(player,
                            Localization.UN_CLAIM__MENU__CONFIRMATION__TITLE.getMessage(),
                            message,
                            Localization.UN_CLAIM__MENU__CONFIRMATION__ACCEPT.getItem(),
                            Localization.UN_CLAIM__MENU__CONFIRMATION__DENY.getItem(),
                            (player, aBoolean) -> {
                                if (aBoolean) {
                                    if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                                        CrashClaim.getPlugin().getDataManager().deleteClaim(claim);
                                    } else {
                                        player.sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage());
                                    }
                                }
                                return "";
                            }, player -> "").open();
                } else {
                    player.sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage());
                    forceClose();
                }
                break;
            case 45:
                if (previousMenu == null){
                    return;
                }
                previousMenu.open();
                break;
        }
    }

    public GUI getPreviousMenu() {
        return previousMenu;
    }
}
