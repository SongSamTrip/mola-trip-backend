package com.mola.global.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static com.mola.domain.tripBoard.tripPost.entity.QTripPost.tripPost;

public class QueryDslUtil {

    public static OrderSpecifier<?> getTripPostOrderSpecifier(Pageable pageable){
        if(!pageable.getSort().isEmpty()) {
            for(Sort.Order order : pageable.getSort()) {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                if("like".equals(order.getProperty())) {
                    return new OrderSpecifier(direction, tripPost.likeCount);
                }else if("date".equals(order.getProperty())) {
                    return new OrderSpecifier(direction, tripPost.createdDate);
                }
            }
        }
        return new OrderSpecifier<>(Order.DESC, tripPost.createdDate);
    }
}
