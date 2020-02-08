package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    private SpecParamMapper specParamMapper;
    @Autowired
    private SpecGroupMapper specGroupMapper;

    public List<SpecParam> querySpecParam(Long cid,Boolean searching) {
        SpecParam param = new SpecParam();
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> specParams = specParamMapper.select(param);
        if(CollectionUtils.isEmpty(specParams)){
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOND);
        }
        return specParams;
    }

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> specGroups = specGroupMapper.select(specGroup);
        if(CollectionUtils.isEmpty(specGroups)){
            throw new LyException(ExceptionEnum.SPEC_GROUD_NOT_FOND);
        }
        for (SpecGroup group : specGroups) {
            SpecParam param = new SpecParam();
            param.setGroupId(group.getId());
            List<SpecParam> select = specParamMapper.select(param);
            if(CollectionUtils.isEmpty(select)){
                throw new LyException(ExceptionEnum.SPEC_GROUD_NOT_FOND);
            }
            group.setParams(select);
        }
        return specGroups;
    }
}
