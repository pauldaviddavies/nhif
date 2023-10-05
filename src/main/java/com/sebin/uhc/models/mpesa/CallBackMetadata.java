package com.sebin.uhc.models.mpesa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CallBackMetadata {
    private List<Item> Item;
}
