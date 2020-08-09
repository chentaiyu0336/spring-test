package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
    RsService rsService;

    @Mock
    RsEventRepository rsEventRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    VoteRepository voteRepository;
    @Mock
    TradeRepository tradeRepository;
    LocalDateTime localDateTime;
    Vote vote;

    @BeforeEach
    void setUp() {
        initMocks(this);
        rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
        localDateTime = LocalDateTime.now();
        vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
    }

    @Test
    void shouldVoteSuccess() {
        // given

        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();

        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
        // when
        rsService.vote(vote, 1);
        // then
        verify(voteRepository)
                .save(
                        VoteDto.builder()
                                .num(2)
                                .localDateTime(localDateTime)
                                .user(userDto)
                                .rsEvent(rsEventDto)
                                .build());
        verify(userRepository).save(userDto);
        verify(rsEventRepository).save(rsEventDto);
    }

    @Test
    void shouldThrowExceptionWhenUserNotExist() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        //when&then
        assertThrows(
                RuntimeException.class,
                () -> {
                    rsService.vote(vote, 1);
                });
    }

    @Test
    void shouldTradeSuccessWhenRsEventHasNotBeenBought() {
        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();
        TradeDto tradeDTO =
                TradeDto.builder()
                        .rsEventDto(rsEventDto)
                        .amount(100)
                        .rank(1)
                        .build();
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
        when(tradeRepository.findById(anyInt())).thenReturn(Optional.of(tradeDTO));

        Trade trade = Trade.builder().amount(tradeDTO.getAmount()).rank(tradeDTO.getRank()).build();
        rsEventRepository.save(rsEventDto);
        userRepository.save(userDto);

        rsService.buy(trade, rsEventDto.getId());

        verify(tradeRepository).save(tradeDTO);
    }

    @Test
    void shouldThrowExceptionWhenMoneyIsNotEnoughToBuyPresentRank() {
        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto1 =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();
        RsEventDto rsEventDto2 =
                RsEventDto.builder()
                        .eventName("event name 2")
                        .id(3)
                        .keyword("keyword 2")
                        .voteNum(3)
                        .user(userDto)
                        .build();
        TradeDto tradeDto1 =
                TradeDto.builder()
                        .rsEventDto(rsEventDto1)
                        .amount(100)
                        .rank(1)
                        .build();

        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto2));
        when(tradeRepository.findTradeDtoByRanking(anyInt())).thenReturn(Optional.of(tradeDto1));

        TradeDto tradeDto2 =
                TradeDto.builder()
                        .rsEventDto(rsEventDto2)
                        .amount(50)
                        .rank(1)
                        .build();
        Trade trade = Trade.builder().amount(tradeDto2.getAmount()).rank(tradeDto2.getRank()).build();

        assertThrows(
                RuntimeException.class, () -> {
                    rsService.buy(trade, rsEventDto2.getId());
                }
        );
    }


    @Test
    void shouldUpdateCurRankWhenAmountIsEnoughToTrade() {
        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto1 =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();
        RsEventDto rsEventDto2 =
                RsEventDto.builder()
                        .eventName("event name 2")
                        .id(3)
                        .keyword("keyword 2")
                        .voteNum(3)
                        .user(userDto)
                        .build();
        TradeDto tradeDto1 =
                TradeDto.builder()
                        .rsEventDto(rsEventDto1)
                        .amount(100)
                        .rank(1)
                        .build();


        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto2));
        when(tradeRepository.findTradeDtoByRanking(anyInt())).thenReturn(Optional.of(tradeDto1));

        TradeDto tradeDto2 =
                TradeDto.builder()
                        .rsEventDto(rsEventDto2)
                        .amount(200)
                        .rank(1)
                        .build();
        Trade trade=Trade.builder().rank(tradeDto2.getRank()).amount(tradeDto2.getAmount()).build();


        rsService.buy(trade,rsEventDto2.getId());

        verify(tradeRepository).delete(tradeDto1);
        verify(tradeRepository).save(tradeDto2);
        verify(rsEventRepository).delete(tradeDto1.getRsEventDto());
        verify(rsEventRepository).save(tradeDto2.getRsEventDto());
    }

}
