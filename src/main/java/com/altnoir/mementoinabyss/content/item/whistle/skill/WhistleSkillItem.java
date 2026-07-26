package com.altnoir.mementoinabyss.content.item.whistle.skill;

import com.altnoir.mementoinabyss.content.item.whistle.fragment.WhistleFragmentItem;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleSkillContext;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleSkillDefinition;

public class WhistleSkillItem extends WhistleFragmentItem<WhistleSkillDefinition> {
    public WhistleSkillItem(Properties properties, WhistleSkillDefinition definition) {
        super(properties, definition);
    }

    public void activate(WhistleSkillContext context) {}
}
