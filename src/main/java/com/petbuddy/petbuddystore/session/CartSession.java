package com.petbuddy.petbuddystore.session;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSession {
    private String userId;

    @Builder.Default
    private List<CartItemSession> items = new ArrayList<>();

    public void initialize(String userId){
        this.userId = userId;
        this.items.clear();
    }

    public void clear(){
        this.userId = null;
        this.items.clear();
    }
}
