package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.block.BlockRepository;
import com.mupol.mupolserver.domain.comment.Block;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.block.BlockResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BlockService {

    private final BlockRepository blockRepository;

    public BlockResDto block(User blocker, User blocked) {

        Block block = Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
        blockRepository.save(block);

        return getBlockDto(block);
    }

    public void unblock(User blocker, User blocked){ blockRepository.deleteByBlockerAndBlocked(blocker, blocked); }

    public BlockResDto getBlockDto(Block block) {

        BlockResDto dto = new BlockResDto();

        dto.setId(block.getId());
        dto.setBlockerId(block.getBlocker().getId());
        dto.setBlockedId(block.getBlocked().getId());
        return dto;
    }

}