package com.subjectmanage.mapper;

import com.subjectmanage.beans.Topic;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TopicMapper {
    public Topic getTopicWithGroupById(int topic_id);

    public List<Topic> selectAll(int startIndex, int pageSize);

    public int getTotal();

    public int addTopic(Topic topic);

    public int updateTopic(Topic topic);

    public int deleteTopic(int topic_id);
}
