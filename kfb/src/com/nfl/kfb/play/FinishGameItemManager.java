/**
 * 
 */
package com.nfl.kfb.play;

import java.util.ArrayList;
import java.util.List;

import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 8. 7.
 *
 */
public class FinishGameItemManager {
	
	List<Inven> updateItems = new ArrayList<Inven>();
	List<Inven> insertItems = new ArrayList<Inven>();
	
	public FinishGameItemManager(InvenMapper invenMapper, String appId) {
		updateItems.addAll(invenMapper.selectAllItem(appId));
	}

	public void addRewardItem(Inven item) {
		Inven existItem = getItem(item.getChId(), item.getItemId());
		if (existItem == null) {
			item.setValueChanged(true);
			insertItems.add(item);
		}
		else {
			final ITEM_TYPE itemType = DebugOption.getItemType(item.getItemId());
			switch (itemType) {
//			case CHARACTER:
//				existItem.setValueChanged(true);
//				existItem.setItemLv(Math.min(DebugOption.MAX_CHARACTER_LV, existItem.getItemLv() + item.getItemLv()));
//				break;
				
			case SKILL:
				existItem.setValueChanged(true);
				existItem.setItemCnt(existItem.getItemCnt() + item.getItemCnt());
				break;
				
			case CONSUME:
				existItem.setValueChanged(true);
				existItem.setItemCnt(existItem.getItemCnt() + item.getItemCnt());
				break;
				
//			case EQUIP:
//				existItem.setValueChanged(true);
//				existItem.setItemLv(existItem.getItemLv() + item.getItemLv());
//				break;
				
//			case PET:
//				existItem.setValueChanged(true);
//				existItem.setItemLv(existItem.getItemLv() + item.getItemLv());
//				break;
				
			default:		// 그외의 아이템은 INVEN에 들어가는 아이템이 아님
				throw new RuntimeException("Cannot add item to inven. this is not finish-game-reward item");
			}
		}
	}

	public void useItem(Inven item) {
		Inven existItem = getItem(item.getChId(), item.getItemId());
		if (existItem == null) {
			// 없는 아이템인데 어떻게 사용을 했지? 버그? 해킹?
			throw new RuntimeException("Use item you don't have. how?, itemChId=" + item.getChId() + ", itemId=" + item.getItemId());
		}
		else {
			final ITEM_TYPE itemType = DebugOption.getItemType(item.getItemId());
			switch (itemType) {
//			case CHARACTER:
//				existItem.setValueChanged(true);
//				existItem.setItemLv(Math.min(DebugOption.MAX_CHARACTER_LV, existItem.getItemLv() + item.getItemLv()));
//				break;
				
			case SKILL:
				if (existItem.getItemCnt() < item.getItemCnt()) {
					throw new RuntimeException("Not enough itemCnt. itemChId=" + item.getChId() + ", itemId=" + item.getItemId()
							+ ", now=" + existItem.getItemCnt() + ", use=" + item.getItemCnt());
				}
				existItem.setValueChanged(true);
				existItem.setItemCnt(existItem.getItemCnt() - item.getItemCnt());
				break;
				
			case CONSUME:
				if (existItem.getItemCnt() < item.getItemCnt()) {
					throw new RuntimeException("Not enough itemCnt. itemChId=" + item.getChId() + ", itemId=" + item.getItemId()
							+ ", now=" + existItem.getItemCnt() + ", use=" + item.getItemCnt());
				}
				existItem.setValueChanged(true);
				existItem.setItemCnt(existItem.getItemCnt() - item.getItemCnt());
				break;
				
//			case EQUIP:
//				existItem.setValueChanged(true);
//				existItem.setItemLv(existItem.getItemLv() + item.getItemLv());
//				break;
				
//			case PET:
//				existItem.setValueChanged(true);
//				existItem.setItemLv(existItem.getItemLv() + item.getItemLv());
//				break;
				
			default:		// 그외의 아이템은 INVEN에 들어가는 아이템이 아님
				throw new RuntimeException("Cannot add item to inven. this is not finish-game-reward item");
			}
		}
	}

	/**
	 * 아이템 조회
	 * @param chId
	 * @param itemId
	 * @return
	 */
	private Inven getItem(int chId, int itemId) {
		for (Inven item : updateItems) {
			if (item.getChId() == chId && item.getItemId() == itemId)
				return item;
		}
		for (Inven item : insertItems) {
			if (item.getChId() == chId && item.getItemId() == itemId)
				return item;
		}
		return null;
	}

	public void commit(InvenMapper invenMapper) {
		for (Inven item : updateItems) {
			if (item.isValueChanged()) {
				invenMapper.updateItem(item);
			}
		}
		for (Inven item : insertItems) {
			if (item.isValueChanged()) {
				invenMapper.insertItem(item);
			}
		}
	}
	
}
