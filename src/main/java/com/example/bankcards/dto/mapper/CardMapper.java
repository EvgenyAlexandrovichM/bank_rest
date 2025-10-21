package com.example.bankcards.dto.mapper;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.entity.card.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "ownerUsername", source = "owner.username")
    @Mapping(target = "cardNumber", source = "encryptedNumber")
    CardDto toDto(Card card);
}