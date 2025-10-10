package net.CoffeDino.testmod.races;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class races {
    private static Race clientRace = null;
    public enum Race {
        SCULK("sculk", "Sculk"),
        WARDER("warder", "Warder"),
        ENDER("ender", "Ender"),
        PHANTOM("phantom", "Phantom");

        private final String id;
        private final String displayName;

        Race(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        public String getId() {
            return id;
        }
        public String getDisplayName(){
            return displayName;
        }
    }
    public static void setPlayerRace(Player player, Race race){
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound(TestingCoffeDinoMod.MOD_ID);
        modData.putString("race", race.getId());
        persistentData.put(TestingCoffeDinoMod.MOD_ID, modData);

        if(player.level().isClientSide()){
            clientRace= race;
        }

        applyRaceEffects(player, race);
    }

    public static Race getPlayerRace(Player player){

        if (player.level().isClientSide() && clientRace !=null){
            return clientRace;
        }
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound(TestingCoffeDinoMod.MOD_ID);
        String raceId = modData.getString("race");
        if (raceId.isEmpty()){
            return null;
        }
        for (Race race : Race.values()){
            if (race.getId().equals(raceId)){
                return race;
            }
        }
        return null;
    }

    public static boolean hasChosenRace(Player player){
        // For client-side checks, we need to be more lenient
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound(TestingCoffeDinoMod.MOD_ID);
        String raceId = modData.getString("race");

        System.out.println("DEBUG hasChosenRace - Player: " + player.getName().getString());
        System.out.println("DEBUG hasChosenRace - Is client side: " + player.level().isClientSide());
        System.out.println("DEBUG hasChosenRace - Race ID in NBT: '" + raceId + "'");
        System.out.println("DEBUG hasChosenRace - Full mod data: " + modData);

        boolean hasRace = !raceId.isEmpty();
        System.out.println("DEBUG hasChosenRace - Result: " + hasRace);

        return hasRace;
    }

    private static void applyRaceEffects(Player player, Race race){
        switch (race){
            case ENDER -> applyEndertraits(player);
            case SCULK -> applySculkTraits(player);
            case WARDER -> applyWarderTraits(player);
            case PHANTOM -> applyPhantomTraits(player);

        }
    }
    private static void applyEndertraits(Player player){

    }
    private static void applySculkTraits(Player player){

    }
    private static void applyWarderTraits(Player player){

    }
    private static void applyPhantomTraits(Player player){

    }


}
