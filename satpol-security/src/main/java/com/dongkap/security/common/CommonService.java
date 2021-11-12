package com.dongkap.security.common;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.google.common.base.CaseFormat;

public class CommonService {;

    protected Pageable page(Map<String, List<String>> order, int offset, int limit) {
        int page = 0;
        if (limit < 10) limit = 10;
        else if (limit > 250) limit = 250;
        page = offset / limit;
        if (page < 0) page = 0;
        if (order != null && !order.isEmpty()) {
            Sort sort = null;
            for (Map.Entry<String, List<String>> direction : order.entrySet()) {
                if (Direction.ASC.toString().equalsIgnoreCase(direction.getKey())) {
                    sort = Sort.by(Direction.ASC, (String[]) direction.getValue().toArray());
                } else {
                    sort = Sort.by(Direction.DESC, (String[]) direction.getValue().toArray());
                }
            }
            return PageRequest.of(page, limit, sort);
        }
        return PageRequest.of(page, limit);
    }

    public String convertToCamelCase(String string) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, string);
    }

    public String convertToLowerUnderscore(String string) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string);
    }
}
