package chr.fotmation;

/**
 * Created by think on 2017/9/12.
 */
public class Formation {
	private long heroId;

	private long[] equipId = new long[6];

	private long petId;

	private long horseId;

	private long mingjiangId;

	public Formation(long heroId) {
		this.heroId = heroId;
	}

	public long getMingjiangId() {
		return mingjiangId;
	}

	public void setMingjiangId(long mingjiangId) {
		this.mingjiangId = mingjiangId;
	}

	public long getHeroId() {
		return heroId;
	}

	public void setHeroId(long heroId) {
		this.heroId = heroId;
	}

	public long[] getEquipId() {
		return equipId;
	}

	public void setEquipId(long[] equipId) {
		this.equipId = equipId;
	}

	public long getPetId() {
		return petId;
	}

	public void setPetId(long petId) {
		this.petId = petId;
	}

	public long getHorseId() {
		return horseId;
	}

	public void setHorseId(long horseId) {
		this.horseId = horseId;
	}
}
