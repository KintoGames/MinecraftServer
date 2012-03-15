package dev.riffic33.heroes.skills;


import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;

public class SkillRocketJump extends ActiveSkill {
		
    public SkillRocketJump(Heroes plugin) {
        super(plugin, "Rocketjump");
        setUsage("/skill rocketjump");
        setArgumentRange(0, 0);
        setIdentifiers("skill rocketjump");
        setTypes(SkillType.MOVEMENT, SkillType.PHYSICAL);
        
        Bukkit.getServer().getPluginManager().registerEvents(new SkillListener(this), plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("rocket-speed", 2);
        node.set("rocket-boosts", 3);
        node.set(Setting.DURATION.node(), 10000);
        node.set(Setting.COOLDOWN.node(), 10000);
        node.set(Setting.REAGENT.node(), "REDSTONE");
        node.set(Setting.REAGENT_COST.node(), 1);
        return node;
    }
    
    @Override
    public String getDescription(Hero hero) {
        int boosts = SkillConfigManager.getUseSetting(hero, this, "rocket-boosts", 1, false);
        return String.format("Put on a rocket pack with %s boosts. Safe fall provided.", boosts);
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        float rocketSpeed = (float) SkillConfigManager.getUseSetting(hero, this, "rocket-speed", 1, false);
        int boosts = (int) SkillConfigManager.getUseSetting(hero, this, "rocket-boosts", 3, false);
        long duration = (int) SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false);
  
        player.setFallDistance(-(rocketSpeed*8F*boosts));
        RocketPack rp = new RocketPack(this, duration, boosts);
        player.sendMessage("Using rocket jump");
        hero.addEffect(rp);
        broadcastExecuteText(hero);

        return SkillResult.NORMAL;
    }
    
    public class RocketPack extends ExpirableEffect{

    	private int boostsLeft = 1;
    	
		public RocketPack(Skill skill, long duration, int boosts) {
			super(skill, "RocketPack", duration);
			this.boostsLeft = boosts;
			this.types.add(EffectType.PHYSICAL);
			this.types.add(EffectType.BENEFICIAL);
		}
		
		@Override
        public void apply(Hero hero) {
            super.apply(hero);

        }
		
		@Override
        public void remove(Hero hero) {
            super.remove(hero);
            Messaging.send(hero.getPlayer(), "Rocket Pack ran out of fuel");
        }
		
		public int getBoostsLeft(){
			return boostsLeft;
		}
		//use and return amount left
		public int useBoost(){
			boostsLeft--;
			return boostsLeft;
		}
    	
    }
    
    public class SkillListener implements Listener{
    	private Skill skill;
    	
    	public SkillListener(Skill skill){
    		this.skill = skill;
    	}
		
    	@EventHandler
    	public void onPlayerInteract(PlayerInteractEvent event){
    		Player player = event.getPlayer();
    		Hero hero = plugin.getHeroManager().getHero(player);
    		if(hero.hasEffect("RocketPack")){
    			float rocketSpeed = (float) SkillConfigManager.getUseSetting(hero, skill, "rocket-speed", 1, false);
    			RocketPack rp = (RocketPack) hero.getEffect("RocketPack");
    			Vector 	newV = player.getLocation().getDirection().clone();
    					newV.normalize().multiply(rocketSpeed);
    			player.setVelocity(newV);
    			Messaging.send(player, "Rocket Pack has $1 boosts left", rp.boostsLeft);
    			if(rp.useBoost() < 1){
    				hero.removeEffect(rp);
    			}
    			
    		}
    		
    	}
    	
    }
    
   
    
    
}