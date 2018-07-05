package cn.mrray.blockchain.core.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author wuweifeng wrote on 2017/10/25.
 */
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Long> {

}
