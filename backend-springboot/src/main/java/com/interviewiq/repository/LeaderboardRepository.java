package com.interviewiq.repository;

import com.interviewiq.model.Leaderboard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends MongoRepository<Leaderboard, String> {
    Optional<Leaderboard> findByUserId(String userId);
    List<Leaderboard> findAllByOrderByXpDesc(Pageable pageable);
    long countByXpGreaterThan(Integer xp);
}
