package chr.hero;

import base.IEntity;
import chr.attr.AttrEntity;
import config.bean.EBattleAttribute;
import config.bean.ECountry;
import config.bean.HeroBase;
import config.provider.HeroInfoProvider;
import define.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.font.EAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/9/8.
 */
public class HeroEntity extends AttrEntity {
	private static Logger logger = LoggerFactory.getLogger(HeroEntity.class);

	private int heroId;

	private HeroBase heroConf;

	private int level;                              //当前等级

	private int breakLevel;                        //突破等级

	private int exp;                                //当前经验

	private int[] awakeInfo = new int[4];

	private int awakeLevel;

	private int tianmingLevel;                    //天命等级

	private int huashenLevel;					//化神等级						橙武将多少， 红武将多少

	private Map<EBattleAttribute, Integer> attributeMap;

	public HeroEntity() {
		super(EntityType.HERO);
		this.attributeMap = new HashMap<>();
		this.level = 1;
	}

	public int getHuashenLevel() {
		return huashenLevel;
	}

	public void setHuashenLevel(int huashenLevel) {
		this.huashenLevel = huashenLevel;
	}

	public int getHeroId() {
		return heroId;
	}

	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}

	public void loadBaseConfig() {
		HeroBase baseConf = HeroInfoProvider.getInst().getHeroBaseConfById(this.heroId);
		if (baseConf == null) {
			logger.warn("create hero with unknow id {}", this.heroId);
			return;
		}
		baseConf.getBattle_attribute().forEach(e -> addAttribute(e.getAttrId(), e.getValue()));
		this.heroConf = baseConf;
	}

	public int[] getAwakeInfo() {
		return awakeInfo;
	}

	public void setAwakeInfo(int[] awakeInfo) {
		this.awakeInfo = awakeInfo;
	}

	public int getAwakeLevel() {
		return awakeLevel;
	}

	public void setAwakeLevel(int awakeLevel) {
		this.awakeLevel = awakeLevel;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getBreakLevel() {
		return breakLevel;
	}

	public long calBattleScore() {
		return attributeMap.get(EBattleAttribute.HP) * 10;
	}

	public void setBreakLevel(int breakLevel) {
		this.breakLevel = breakLevel;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getTianmingLevel() {
		return tianmingLevel;
	}

	public void setTianmingLevel(int tianmingLevel) {
		this.tianmingLevel = tianmingLevel;
	}

	public void loadLevelAttribute() {
	}

	public void loadBreakAttribute() {

	}

	public void loadAwakeAttribute() {

	}

	//计算属性还是蛮烦的
	public void reloadAttribute(){

	}

	public static HeroEntity getEmptyEntity() {
		return new HeroEntity();
	}

	public static HeroEntity createHeroEntity(long entityId, int heroId) {
		//创建hero的工厂方法
		HeroEntity heroEntity = new HeroEntity();
		heroEntity.setEntityId(entityId);
		heroEntity.setHeroId(heroId);
		heroEntity.loadBaseConfig();
		return heroEntity;
	}
}
