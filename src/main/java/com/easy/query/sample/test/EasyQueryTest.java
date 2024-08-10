package com.easy.query.sample.test;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.easy.query.api.proxy.base.MapTypeProxy;
import com.easy.query.api.proxy.client.DefaultEasyEntityQuery;
import com.easy.query.api.proxy.client.DefaultEasyProxyQuery;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.api.proxy.key.MapKey;
import com.easy.query.api.proxy.key.MapKeys;
import com.easy.query.api4j.client.DefaultEasyQuery;
import com.easy.query.core.api.client.EasyQueryClient;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.basic.extension.track.TrackManager;
import com.easy.query.core.basic.jdbc.tx.Transaction;
import com.easy.query.core.bootstrapper.EasyQueryBootstrapper;
import com.easy.query.core.configuration.QueryConfiguration;
import com.easy.query.core.context.QueryRuntimeContext;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.core.exception.EasyQueryInvalidOperationException;
import com.easy.query.core.exception.EasyQuerySingleMoreElementException;
import com.easy.query.core.expression.builder.core.NotNullOrEmptyValueFilter;
import com.easy.query.core.func.def.enums.OrderByModeEnum;
import com.easy.query.core.logging.LogFactory;
import com.easy.query.core.proxy.core.draft.Draft2;
import com.easy.query.core.proxy.core.draft.Draft3;
import com.easy.query.core.proxy.sql.GroupKeys;
import com.easy.query.core.proxy.sql.Select;
import com.easy.query.h2.config.H2DatabaseConfiguration;
import com.easy.query.sample.base.Config;
import com.easy.query.sample.entity.Company;
import com.easy.query.sample.entity.Permission;
import com.easy.query.sample.entity.Product;
import com.easy.query.sample.entity.User;
import com.easy.query.sample.entity.UserDetail;
import com.easy.query.sample.entity.UserGroup;
import com.easy.query.sample.entity.proxy.UserProxy;
import com.easy.query.sample.page.CustomPager;
import com.easy.query.sample.page.PageResult;
import com.easy.query.sample.plugin.CustomLogicDelStrategy;
import com.easy.query.sample.plugin.UserNavigateExtraFilterStrategy;
import com.easy.query.sample.vo.UserDetailVo;
import com.easy.query.sample.vo.UserVo;
import com.easy.query.sample.vo.proxy.UserDetailVoProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hoysing
 * @date 2024-07-08 16:19
 * @since 1.0.0
 */
public class EasyQueryTest {

    private static EasyEntityQuery easyEntityQuery;

    @BeforeAll
    public static void setUp() {
        DataSource dataSource = Config.getDataSource();
        //采用控制台输出打印sql
        LogFactory.useStdOutLogging();
        //property的api
        EasyQueryClient easyQueryClient = EasyQueryBootstrapper.defaultBuilderConfiguration()
                .setDefaultDataSource(dataSource)
                .optionConfigure(op -> {
                    op.setPrintSql(true);
                    op.setKeepNativeStyle(true);
                    op.setDefaultTrack(true);
                })
                .useDatabaseConfigure(new H2DatabaseConfiguration())
                .build();

        easyEntityQuery = new DefaultEasyEntityQuery(easyQueryClient);
        QueryRuntimeContext runtimeContext = easyEntityQuery.getRuntimeContext();
        QueryConfiguration queryConfiguration = runtimeContext.getQueryConfiguration();
        queryConfiguration.applyNavigateExtraFilterStrategy(new UserNavigateExtraFilterStrategy());
        queryConfiguration.applyLogicDeleteStrategy(new CustomLogicDelStrategy());
    }

    @Test
    public void testQueryAll() {
        //查询全部
        List<User> users = easyEntityQuery.queryable(User.class).toList();
        Assertions.assertTrue(users.size() > 0);
    }

    @Test
    public void testQueryColumns() {
        //查询指定列名
        List<User> users = easyEntityQuery.queryable(User.class)
                .select(u -> u.FETCHER.id().name().fetchProxy()).toList();
        for (User user : users) {
            Assertions.assertNotNull(user.getId());
            Assertions.assertNotNull(user.getName());
            Assertions.assertNull(user.getCreateTime());
            Assertions.assertNull(user.getUpdateTime());
        }

        users = easyEntityQuery.queryable(User.class)
                .select(User.class, u -> Select.of(u.FETCHER.allFieldsExclude(u.createTime(), u.updateTime()))).toList();
        for (User user : users) {
            Assertions.assertNotNull(user.getId());
            Assertions.assertNotNull(user.getName());
            Assertions.assertNull(user.getCreateTime());
            Assertions.assertNull(user.getUpdateTime());
        }

        users = easyEntityQuery.queryable(User.class)
                .select(o -> new UserProxy()
                        .selectAll(o)
                        .selectIgnores(o.createTime(), o.updateTime())
                ).toList();
        for (User user : users) {
            Assertions.assertNotNull(user.getId());
            Assertions.assertNotNull(user.getName());
            Assertions.assertNull(user.getCreateTime());
            Assertions.assertNull(user.getUpdateTime());
        }
    }

    @Test
    public void testConditionQuery() {
        //假设firstName和lastName是用户输入的值
        String firstName = "张";
        String lastName = "三";
        Date startTime = DateUtil.parse("2020-01-01");
        Date endTime = DateUtil.parse("2050-01-01");
        //条件查询
        List<User> users = easyEntityQuery.queryable(User.class)
                .where(u -> {
                    //AND name LIKE '%张%'
                    u.name().like(firstName);
                    //AND name LIKE '张%'
                    u.name().likeMatchLeft(firstName);
                    //AND name LIKE '%三'
                    u.name().likeMatchRight(lastName);
                    //AND '2020-01-01' <= create_time AND create_time <= '2050-01-01'
                    u.createTime().rangeClosed(startTime, endTime);
                    //AND company_id IS NOT NULL
                    u.companyId().isNotNull();
                })
                .toList();
        Assertions.assertTrue(users.size() > 0);

        //动态条件查询，只有非空条件才会加到SQL
        users = easyEntityQuery.queryable(User.class)
                .where(u -> {
                    //AND name LIKE '%张%'
                    u.name().like(!ObjectUtil.isEmpty(firstName), firstName);
                    //AND name LIKE '张%'
                    u.name().likeMatchLeft(!ObjectUtil.isEmpty(firstName), firstName);
                    //AND name LIKE '%三'
                    u.name().likeMatchRight(!ObjectUtil.isEmpty(lastName), lastName);
                    //AND '2020-01-01' <= create_time AND create_time <= '2050-01-01'
                    u.createTime().rangeClosed(!ObjectUtil.isEmpty(startTime), startTime, !ObjectUtil.isEmpty(endTime), endTime);
                    //AND company_id IS NOT NULL
                    u.companyId().isNotNull();
                })
                .toList();
        Assertions.assertTrue(users.size() > 0);

        //前面一个一个拼接过于麻烦,可以使用NotNullOrEmptyValueFilter.DEFAULT
        users = easyEntityQuery.queryable(User.class)
                //当传入的条件参数值非空时才会增加到条件里面,也就是说无需再一个一个使用!ObjectUtil.isEmpty(firstName)判断
                //注意只有where的条件生效。当查询的属性不使用函数时才会生效，否则无效
                .filterConfigure(NotNullOrEmptyValueFilter.DEFAULT)
                .where(u -> {
                    //AND name LIKE '%张%'
                    u.name().like(firstName);
                    //AND name LIKE '张%'
                    u.name().likeMatchLeft(firstName);
                    //AND name LIKE '%三'
                    u.name().likeMatchRight(lastName);
                    //AND '2020-01-01' <= create_time AND create_time <= '2050-01-01'
                    u.createTime().rangeClosed(startTime, endTime);
                    //AND company_id IS NOT NULL
                    u.companyId().isNotNull();
                })
                .toList();
        Assertions.assertTrue(users.size() > 0);
    }


    @Test
    public void testPage() {
        //查询分页
        EasyPageResult<User> pageResult = easyEntityQuery.queryable(User.class).toPageResult(1, 20);
        Assertions.assertTrue(pageResult.getData().size() > 0);
        Assertions.assertTrue(pageResult.getTotal() > 0);
    }

    @Test
    public void testCustomPage() {
        //自定义PageResult
        PageResult<User> customerPageResult = easyEntityQuery
                .queryable(User.class)
                .whereById("1")
                .toPageResult(new CustomPager<>(1, 2));
        Assertions.assertTrue(customerPageResult.getList().size() > 0);
        Assertions.assertTrue(customerPageResult.getTotalCount() > 0);
    }

    @Test
    public void testOrder() {
        //排序
        List<User> users = easyEntityQuery.queryable(User.class)
                .orderBy(u -> {
                    u.createTime().desc();
                    u.balance().asc();
                }).toList();
        Assertions.assertTrue(users.size() > 0);

        //排序
        easyEntityQuery.queryable(User.class)
                .orderBy(u -> {
                    //NULL排后面
                    u.createTime().asc(OrderByModeEnum.NULLS_LAST);
                    //NULL排前面
                    u.balance().desc(OrderByModeEnum.NULLS_FIRST);
                }).toList();
        Assertions.assertTrue(users.size() > 0);
    }

    @Test
    public void testGroup() {
        //查询每个公司的用户数
        List<Draft2<Integer, Long>> drafts = easyEntityQuery.queryable(User.class)
                .groupBy(u -> GroupKeys.TABLE1.of(u.companyId()))
                .having(group -> group.count().eq(1L))
                .select(group -> Select.DRAFT.of(
                        //此处的key1就是分组的companyId
                        group.key1(),
                        group.count()
                )).toList();
        for (Draft2<Integer, Long> draft : drafts) {
            Long count = draft.getValue2();
            Assertions.assertTrue(count >= 1L);
        }

        //查询每个公司的用户数，自定义类型
        List<UserGroup> userGroups = easyEntityQuery.queryable(User.class)
                .groupBy(u -> GroupKeys.TABLE1.of(u.companyId()))
                .having(group -> group.groupTable().createTime().max().le(new Date()))
                .select(UserGroup.class, group -> Select.of(
                        group.groupTable().companyId().as(UserGroup::getCompanyId),
                        group.count().as(UserGroup::getCount)
                )).toList();
        for (UserGroup userGroup : userGroups) {
            Integer count = userGroup.getCount();
            Assertions.assertTrue(count >= 1L);
        }
    }


    @Test
    public void testId() {
        Integer id = 1;
        //根据id查询，返回列表
        List<User> users = easyEntityQuery.queryable(User.class)
                .where(e -> e.id().eq(1))
                .toList();
        Assertions.assertTrue(users.size() > 0);

        //主键查询：根据id查询第一条记录，允许为空
        User idUser = easyEntityQuery.queryable(User.class)
                .findOrNull(id);
        Assertions.assertNotNull(idUser);

        //主键查询：根据id查询第一条记录，不允许为空
        idUser = easyEntityQuery.queryable(User.class)
                .findNotNull(id);
        Assertions.assertNotNull(idUser);

        //条件查询：根据id查询第一条记录，允许为空
        idUser = easyEntityQuery.queryable(User.class)
                .whereById(id)
                .firstOrNull();
        Assertions.assertNotNull(idUser);

        //条件查询：根据id查询第一条记录，不允许为空
        idUser = easyEntityQuery.queryable(User.class)
                .whereById(id)
                .firstNotNull();
        Assertions.assertNotNull(idUser);

        //条件查询：根据id只查询一条记录，允许为空，如果结果有多条记录，则抛出EasyQuerySingleMoreElementException
        idUser = easyEntityQuery.queryable(User.class)
                .whereById(id)
                .singleOrNull();
        Assertions.assertNotNull(idUser);

        //条件查询：根据id只查询一条记录，允许为空，如果结果有多条记录，则抛出EasyQuerySingleMoreElementException
        idUser = easyEntityQuery.queryable(User.class)
                .whereById(id)
                .singleNotNull();
        Assertions.assertNotNull(idUser);
    }

    @Test
    public void testOne() {
        //查询第一条
        User firstUser = easyEntityQuery.queryable(User.class).firstOrNull();
        Assertions.assertNotNull(firstUser);

        Assertions.assertThrows(EasyQuerySingleMoreElementException.class, () -> {
            //只查询一条，如果有多条则抛出异常
            easyEntityQuery.queryable(User.class).singleOrNull();
        });

        //判断是否存在
        boolean exists = easyEntityQuery.queryable(User.class).where(u -> u.name().eq("张三")).any();
        Assertions.assertTrue(exists);
    }

    @Test
    public void testAgg() {
        long count = easyEntityQuery.queryable(User.class).count();
        Assertions.assertTrue(count > 0);
        int intCount = easyEntityQuery.queryable(User.class).intCount();
        Assertions.assertTrue(intCount > 0);


        BigDecimal sumBalance = easyEntityQuery.queryable(User.class).sumOrNull(o -> o.balance());
        Assertions.assertNotNull(sumBalance);

        sumBalance = easyEntityQuery.queryable(User.class).sumOrDefault(o -> o.balance(), BigDecimal.ZERO);
        Assertions.assertNotNull(sumBalance);

        sumBalance = easyEntityQuery.queryable(User.class).sumBigDecimalOrNull(o -> o.balance());
        Assertions.assertNotNull(sumBalance);

        //数字类型使用BigDecimal汇总
        sumBalance = easyEntityQuery.queryable(User.class).sumBigDecimalOrDefault(o -> o.balance(), BigDecimal.ZERO);
        Assertions.assertNotNull(sumBalance);

        //数字类型使用BigDecimal汇总
        easyEntityQuery.queryable(User.class).sumOrDefault(o -> o.balance(), BigDecimal.ZERO);
        Assertions.assertNotNull(sumBalance);
    }

    @Test
    public void testExplicitSubQuery() {
        //查询存在张三用户的公司
        List<Company> companyList = easyEntityQuery.queryable(Company.class)
                .where(c -> {
                    c.id().in(
                            easyEntityQuery.queryable(User.class)
                                    .where(u -> u.name().eq("张三"))
                                    .select(u -> u.companyId())
                    );
                }).toList();
        Assertions.assertTrue(companyList.size() > 0);

        //查询存在张三用户的公司
        companyList = easyEntityQuery.queryable(Company.class)
                .where(c -> {
                    c.expression().exists(() ->
                            easyEntityQuery.queryable(User.class)
                                    .where(u -> {
                                        u.companyId().eq(c.id());
                                        u.name().eq("张三");
                                    })
                    );
                }).toList();
        Assertions.assertTrue(companyList.size() > 0);
    }


    @Test
    public void testSubQuery() {
        //查询存在姓张用户的公司
        List<Company> companyList = easyEntityQuery.queryable(Company.class)
                .where(c -> {
                    c.users().where(u -> {
                        u.name().eq("张三");
                    }).any();
                }).toList();
        Assertions.assertTrue(companyList.size() > 0);

        //查询存在姓张用户的公司，与上面写法效果一样，如果将any方法替换为none方法则用于查询不存在存在姓张用户的公司
        companyList = easyEntityQuery.queryable(Company.class)
                .where(c -> {
                    c.users().any(u -> {
                        u.name().eq("张三");
                    });
                }).toList();
        Assertions.assertTrue(companyList.size() > 0);

        //查询存在姓张用户的公司，与上面写法效果一样
        //联级穿透 flatElement后仅支持但条件判断,多条件会生成多个Exists函数
        //所以如果存在多条件还是建议使用where来处理 flatElement支持多层级穿透
        companyList = easyEntityQuery.queryable(Company.class)
                .where(c -> {
                    //展开users集合穿透到下方直接判断名称
                    c.users().flatElement().name().like("");
                }).toList();
        Assertions.assertTrue(companyList.size() > 0);

        //查询只有一个张三用户的公司
        companyList = easyEntityQuery.queryable(Company.class)
                .where(c -> {
                    c.users().where(u -> {
                        u.name().eq("张三");
                    }).count().eq(1L);
                }).toList();
        Assertions.assertTrue(companyList.size() > 0);

        //查询一个用户签名为静水流深的公司
        easyEntityQuery.queryable(Company.class)
                .where(c -> {
                    c.users().where(u -> {
                        u.userDetail().signature().eq("静水流深");
                    }).count().eq(1L);
                }).toList();
        Assertions.assertTrue(companyList.size() > 0);
    }

    @Test
    public void testOneToOneQuery() {
        //查询用户
        List<User> users = easyEntityQuery.queryable(User.class)
                .include(u -> u.userDetail())
                .include(u -> u.company(), cq -> {
                    //cq是公司查询，c是公司，在这里可以再关联查询出公司详情
                    cq.include(c -> c.companyDetail());
                })
                .where(u -> {
                    u.name().eq("张三");
                }).toList();
        Assertions.assertTrue(users.size() > 0);
        for (User user : users) {
            Assertions.assertNotNull(user.getUserDetail());
            Assertions.assertNotNull(user.getCompany());
            Assertions.assertNotNull(user.getCompany().getCompanyDetail());
        }

        //查询公司在广州市番禺区的用户
        users = easyEntityQuery.queryable(User.class)
                .where(u -> u.company().companyDetail().address().eq("广州市番禺区")).toList();
        Assertions.assertTrue(users.size() > 0);

        //查询公司在广州市的用户
        users = easyEntityQuery.queryable(Company.class)
                //先查出广州的公司
                .where(c -> c.companyDetail().address().eq("广州市番禺区"))
                //最后查出公司查出每个公司的用户列表，因为需要将每个用户列表整合为一个用户列表，因此需要将每个用户列表展开
                .toList(c -> c.users().flatElement());
        Assertions.assertTrue(users.size() > 0);
    }

    @Test
    public void testOneToManyQuery() {
        //使用includes获取一对多关联的用户
        List<Company> companyList = easyEntityQuery.queryable(Company.class)
                .includes(c -> c.users(), uq -> {
                    uq.include(u -> u.userDetail())
                            //当前公司关联的张三用户，如果不加条件就返回当前公司关联的所有用户
                            .where(u -> u.name().eq("张三"));
                })
                .where(c -> {
                    //只查询存在张三用户的公司
                    c.users().where(u -> {
                        u.name().eq("张三");
                    });
                }).toList();
        Assertions.assertTrue(companyList.size() > 0);
        for (Company company : companyList) {
            List<User> users = company.getUsers();
            Assertions.assertNotNull(users);
            for (User user : users) {
                UserDetail userDetail = user.getUserDetail();
                Assertions.assertNotNull(userDetail);
            }
        }
    }

    @Test
    public void testNavigateExtraFilterStrategy() {
        //只查询存在张三用户而且用户是启用状态的公司
        List<Company> companyList = easyEntityQuery.queryable(Company.class)
                //当前公司关联的已启用的用户，因为类级别上加了的额外查询条件
                .includes(c -> c.enabledUsers(), uq -> {
                    uq.include(u -> u.userDetail())
                            //当前公司关联的张三用户，并且用户是启用的，如果不加条件就返回当前公司关联的已启用的用户
                            .where(u -> u.name().eq("张三"));
                })
                .where(c -> c.enabledUsers().any(u -> {
                    u.name().eq("张三");
                }))
                .toList();
        Assertions.assertTrue(companyList.size() > 0);
        for (Company company : companyList) {
            List<User> enabledUsers = company.getEnabledUsers();
            Assertions.assertNotNull(enabledUsers);
            for (User enabledUser : enabledUsers) {
                UserDetail userDetail = enabledUser.getUserDetail();
                Assertions.assertNotNull(userDetail);
            }
        }
    }

    @Test
    public void testManyToManyQuery() {
        //用户为主表，查询用户的权限，扁平化查询结果
        List<Integer> permissionIds = easyEntityQuery.queryable(User.class)
                .where(u -> {
                    u.name().eq("张三");
                })
                .toList(uq -> uq.roles().flatElement().permissions().flatElement().id());
        Assertions.assertTrue(permissionIds.size() > 0);

        //用户为主表，查询用户的权限，扁平化查询结果
        List<Permission> permissions = easyEntityQuery.queryable(User.class)
                .where(u -> {
                    u.name().eq("张三");
                })
                .toList(uq -> uq.roles().flatElement().permissions().flatElement());
        Assertions.assertTrue(permissions.size() > 0);
        //用户为主表，查询用户的权限,查询指定列名
        permissions = easyEntityQuery.queryable(User.class)
                .where(u -> {
                    u.name().eq("张三");
                })
                .toList(uq -> uq.roles().flatElement().permissions().flatElement(p -> p.FETCHER.id().name()));
        Assertions.assertTrue(permissions.size() > 0);

        //权限为主表，查询用户的权限，根据所属用户进行条件查询
        permissions = easyEntityQuery.queryable(Permission.class)
                .where(u -> {
                    u.roles().any(role -> {
                        role.users().any(user -> {
                            user.name().eq("张三");
                        });
                    });
                }).toList();
        Assertions.assertTrue(permissions.size() > 0);
        //权限为主表，查询用户的权限，根据扁平化的所属用户进行条件查询
        permissions = easyEntityQuery.queryable(Permission.class)
                .where(u -> {
                    u.roles().flatElement().users().any(user -> {
                        user.name().eq("张三");
                    });
                }).toList();
        Assertions.assertTrue(permissions.size() > 0);
    }

    @Test
    public void testLeftJoin() {
        List<User> users = easyEntityQuery.queryable(User.class)
                .leftJoin(UserDetail.class, (u, ud) -> u.id().eq(ud.userId()))
                .where((u, ud) -> {
                    u.name().eq("张三");
                    ud.signature().like("静水流深");
                })
                .toList();
        Assertions.assertTrue(users.size() > 0);

        users = easyEntityQuery.queryable(User.class)
                .leftJoin(UserDetail.class, (u, ud) -> u.id().eq(ud.userId()))
                .leftJoin(Company.class, (u, ud, c) -> u.companyId().eq(c.id()))
                .where((u, ud, c) -> {
                    u.name().eq("张三");
                    ud.signature().like("静水流深");
                    c.name().eq("总公司");
                })
                .toList();
        Assertions.assertTrue(users.size() > 0);
    }

    @Test
    public void testQueryReturnType() {
        //查询时，如果没有声明查询结果的返回类型，可以使用Draft类型作为返回类型
        List<Draft3<Integer, String, String>> draftList = easyEntityQuery.queryable(User.class)
                .leftJoin(UserDetail.class, (u, ud) -> u.id().eq(ud.userId()))
                .leftJoin(Company.class, (u, ud, c) -> u.companyId().eq(c.id()))
                .where((u, ud, c) -> {
                    u.name().eq("张三");
                    ud.signature().like("静水流深");
                    c.name().eq("总公司");
                }).select((u, ud, c) -> Select.DRAFT.of(
                        u.id(), ud.signature(), c.name()
                )).toList();
        for (Draft3<Integer, String, String> draft : draftList) {
            Integer userId = draft.getValue1();
            Assertions.assertNotNull(userId);
            String signature = draft.getValue2();
            Assertions.assertNotNull(signature);
            String companyName = draft.getValue3();
            Assertions.assertNotNull(companyName);
        }

        //查询时，如果没有声明查询结果的返回类型，可以使用Draft类型作为返回类型
        MapKey<Integer> userIdKey = MapKeys.integerKey("userId");
        MapKey<String> signatureKey = MapKeys.stringKey("signature");
        MapKey<String> companyNameKey = MapKeys.stringKey("companyName");
        MapKey<Integer> companyIdKey = MapKeys.integerKey("companyId");


        draftList = easyEntityQuery.queryable(User.class)
                .leftJoin(UserDetail.class, (u, ud) -> u.id().eq(ud.userId()))
                .where((u, ud) -> {
                    u.name().eq("张三");
                    ud.signature().like("静水流深");
                })
                .select((u, ud) -> {
                    MapTypeProxy map = new MapTypeProxy();
                    map.put(userIdKey, u.id());
                    map.put(signatureKey, ud.signature());
                    map.put(companyIdKey, u.companyId());
                    return map;
                })
                .leftJoin(Company.class, (uud, c) -> uud.get(companyIdKey).eq(c.id()))
                .select((uud, c) -> Select.DRAFT.of(
                        uud.get(userIdKey),
                        uud.get(signatureKey),
                        c.name()
                )).toList();
        for (Draft3<Integer, String, String> draft : draftList) {
            Integer userId = draft.getValue1();
            Assertions.assertNotNull(userId);
            String signature = draft.getValue2();
            Assertions.assertNotNull(signature);
            String companyName = draft.getValue3();
            Assertions.assertNotNull(companyName);
        }

        List<Map<String, Object>> resultMaps = easyEntityQuery.queryable(User.class)
                .leftJoin(UserDetail.class, (u, ud) -> u.id().eq(ud.userId()))
                .leftJoin(Company.class, (u, ud, c) -> u.companyId().eq(c.id()))
                .where((u, ud, c) -> {
                    u.name().eq("张三");
                    ud.signature().like("静水流深");
                    c.name().eq("总公司");
                }).select((u, ud, c) -> {
                    MapTypeProxy map = new MapTypeProxy();
                    map.put(userIdKey, u.id());
                    map.put(signatureKey, ud.signature());
                    map.put(companyNameKey, c.name());
                    return map;
                }).toList();
        for (Map<String, Object> resultMap : resultMaps) {
            Integer userId = (Integer) resultMap.get("userId");
            Assertions.assertNotNull(userId);
            String signature = (String) resultMap.get("signature");
            Assertions.assertNotNull(signature);
            String companyName = (String) resultMap.get("companyName");
            Assertions.assertNotNull(companyName);
        }
    }

    @Test
    public void testCustomQueryReturnType() {
        //使用指定的类型作为返回类型，默认为匹配的字段设值
        List<UserDetailVo> userDetailVos = easyEntityQuery.queryable(User.class)
                .where(u -> u.name().eq("张三"))
                .select(UserDetailVo.class).toList();
        for (UserDetailVo userDetailVo : userDetailVos) {
            Assertions.assertNotNull(userDetailVo.getId());
            Assertions.assertNotNull(userDetailVo.getName());
            Assertions.assertNull(userDetailVo.getSignature());
        }

        //使用指定的类型作为返回类型，需要手动设值
        userDetailVos = easyEntityQuery.queryable(User.class)
                .where(u -> u.name().eq("张三"))
                .select(UserDetailVo.class, u -> Select.of(
                        //手动为匹配的字段设值,与allFields相似的方法有allFieldsExclude方法
                        u.FETCHER.allFields(),
                        //手动为不匹配的字段设值,as支持传入字段名称
                        u.userDetail().signature().as(UserDetailVo::getSignature)
                )).toList();
        for (UserDetailVo userDetailVo : userDetailVos) {
            Assertions.assertNotNull(userDetailVo.getId());
            Assertions.assertNotNull(userDetailVo.getName());
            Assertions.assertNotNull(userDetailVo.getSignature());
        }

        //查询VO对象时自动查询关联的对象
        //注意自动筛选返回结构化数据,VO和对应的实体类的字段是一样的，比如User有userDetail和roles两个关联对象，那么UserVo也只能声明这些需要自动查询的关联对象
        List<UserVo> userVoList = easyEntityQuery.queryable(User.class)
                .where(u -> u.name().eq("张三"))
                .selectAutoInclude(UserVo.class)
                .toList();
        Assertions.assertTrue(userVoList.size() > 0);

        List<UserDetailVo> userDetailVoList = easyEntityQuery.queryable(User.class)
                .leftJoin(UserDetail.class, (u, ud) -> u.id().eq(ud.userId()))
                .where(u -> u.name().eq("张三"))
                .selectAutoInclude(UserDetailVo.class, (u, ud) -> Select.of(
                        //u.FETCHER.allFields(),请注意,调用select需要加此行,调用selectAutoInclude不需要加此行，因为selectAutoInclude会自动执行allFields
                        u.userDetail().signature().as(UserDetailVo::getSignature)
                ))
                .toList();
        Assertions.assertTrue(userDetailVoList.size() > 0);
    }

    @Test
    public void testCustomQueryReturnTypeWithProxy() {
        //使用指定的类型作为返回类型，需要手动为对应的Proxy设值，注意不需要指定实体类型
        List<UserDetailVo> userDetailVos = easyEntityQuery.queryable(User.class)
                .where(u -> u.name().eq("张三"))
                .select(u ->
                        // Proxy支持selectAll方法和selectIgnore方法
                        new UserDetailVoProxy()
                                .id().set(u.id())
                                .name().set(u.name())
                                .signature().set(u.userDetail().signature())
                )
                .toList();
        for (UserDetailVo userDetailVo : userDetailVos) {
            Assertions.assertNotNull(userDetailVo.getId());
            Assertions.assertNotNull(userDetailVo.getName());
            Assertions.assertNotNull(userDetailVo.getSignature());
        }

        //效果同上
        userDetailVos = easyEntityQuery.queryable(User.class)
                .where(u -> u.name().eq("张三"))
                .select(u -> {
                    UserDetailVoProxy userDetailVoProxy = new UserDetailVoProxy();
                    userDetailVoProxy.id().set(u.id());
                    userDetailVoProxy.name().set(u.name());
                    userDetailVoProxy.signature().set(u.userDetail().signature());
                    return userDetailVoProxy;
                })
                .toList();
        for (UserDetailVo userDetailVo : userDetailVos) {
            Assertions.assertNotNull(userDetailVo.getId());
            Assertions.assertNotNull(userDetailVo.getName());
            Assertions.assertNotNull(userDetailVo.getSignature());
        }

        //效果同上
        userDetailVos = easyEntityQuery.queryable(User.class)
                .where(u -> u.name().eq("张三"))
                .select(u -> new UserDetailVoProxy()
                        .selectExpression(u.id(), u.name(), u.userDetail().signature())
                )
                .toList();
        for (UserDetailVo userDetailVo : userDetailVos) {
            Assertions.assertNotNull(userDetailVo.getId());
            Assertions.assertNotNull(userDetailVo.getName());
            Assertions.assertNotNull(userDetailVo.getSignature());
        }

        List<Draft3<Integer, String, String>> draftList = easyEntityQuery.queryable(User.class)
                .where(u -> u.name().eq("张三"))
                .select(u ->
                        // Proxy支持selectAll方法和selectIgnore方法
                        new UserDetailVoProxy()
                                .id().set(u.id())
                                .name().set(u.name())
                                .signature().set(u.userDetail().signature())
                )
                .leftJoin(UserDetail.class, (u, ud) -> u.id().eq(ud.userId()))
                .select((u, ud) -> Select.DRAFT.of(
                        u.id(), u.name(), ud.signature()
                ))
                .toList();
        for (Draft3<Integer, String, String> draft : draftList) {
            Integer userId = draft.getValue1();
            Assertions.assertNotNull(userId);
            String name = draft.getValue2();
            Assertions.assertNotNull(name);
            String signature = draft.getValue3();
            Assertions.assertNotNull(signature);
        }
    }

    @Test
    public void testInsert() {
        User newUser = new User();
        newUser.setName("新用户");
        newUser.setCreateTime(new Date());
        //插入单条数据
        long rows = easyEntityQuery.insertable(newUser).executeRows(true);
        Assertions.assertTrue(rows > 0);
        Assertions.assertNotNull(newUser.getId());
        User copyUser = new User();
        copyUser.setName("新用户");
        copyUser.setCreateTime(new Date());
        List<User> users = Arrays.asList(newUser, copyUser);
        //插入多条数据
        rows = easyEntityQuery.insertable(users).executeRows(true);
        Assertions.assertTrue(rows > 0);
        for (User user : users) {
            Assertions.assertNotNull(user.getId());
        }
        //批量插入多条数据
        easyEntityQuery.insertable(users).batch().executeRows(true);
        for (User user : users) {
            Assertions.assertNotNull(user.getId());
        }
    }

    @Test
    public void testInsertAllColumns() {
        User user = new User();
        user.setName("新用户");
        user.setCreateTime(new Date());
        long rows = easyEntityQuery.insertable(user).setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS).executeRows(true);
        Assertions.assertTrue(rows > 0);
        Assertions.assertNotNull(user.getId());
    }

    @Test
    public void testInsertMap() {
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("name", "小明");
        userMap.put("create_time", new Date());
        userMap.put("enabled", true);
        long rows = easyEntityQuery.mapInsertable(userMap).asTable("user").executeRows(true);
        Assertions.assertTrue(rows > 0);
        Assertions.assertNull(userMap.get("id"));
    }

    @Test
    public void testUpdate() {
        User existUser = easyEntityQuery.queryable(User.class).findNotNull(1);
        DateTime updateTime = new DateTime();
        existUser.setUpdateTime(updateTime);
        //更新单条数据
        long rows = easyEntityQuery.updatable(existUser).executeRows();
        Assertions.assertTrue(rows > 0);
        List<User> users = easyEntityQuery.queryable(User.class).where(u -> u.id().in(Arrays.asList(1, 2))).toList();
        for (User user : users) {
            user.setUpdateTime(updateTime);
        }
        //更新多条数据
        rows = easyEntityQuery.updatable(users).executeRows();
        Assertions.assertTrue(rows > 0);
        //批量更新多条数据
        rows = easyEntityQuery.updatable(users).batch().executeRows();
        Assertions.assertEquals(users.size(), rows);
    }

    @Test
    public void testUpdateAll() {
        User user = easyEntityQuery.queryable(User.class).findNotNull(1);
        DateTime updateTime = new DateTime();
        user.setUpdateTime(updateTime);
        //更新，默认只更新有值的列，设置SQLExecuteStrategyEnum.ALL_COLUMNS可以更新全部列
        long rows = easyEntityQuery.updatable(user)
                .setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS)
                .executeRows();
        Assertions.assertTrue(rows > 0);
        Assertions.assertNotNull(user.getId());
    }

    @Test
    public void testUpdateCustomColumn() {
        User user = easyEntityQuery.queryable(User.class).findNotNull(1);
        DateTime updateTime = new DateTime();
        user.setUpdateTime(updateTime);
        //更新指定列
        updateTime.offset(DateField.SECOND, 1);
        long rows = easyEntityQuery.updatable(user)
                .setColumns(o -> o.updateTime())//多个字段使用FETCHER.setColumns(o->o.FETCHER.name().updateTime())
                .whereColumns(o -> o.id())//多个字段使用FETCHER.whereColumns(o->o.FETCHER.id().name())
                .executeRows();
        Assertions.assertTrue(rows > 0);

        //更新指定列
        updateTime.offset(DateField.SECOND, 1);
        rows = easyEntityQuery.updatable(User.class)
                .setColumns(o -> {
                    o.updateTime().set(updateTime);
                })
                .where(o -> o.id().eq(user.getId()))
                .executeRows();
        Assertions.assertTrue(rows > 0);

        //更新指定列并断言
        updateTime.offset(DateField.SECOND, 1);
        easyEntityQuery.updatable(User.class)
                .setColumns(o -> {
                    o.updateTime().set(updateTime);
                })
                .where(o -> o.id().eq(user.getId()))
                .executeRows(1, "更新失败");
    }

    @Test
    public void testUpdateColumnType() {
        //自动转换类型
        long rows = easyEntityQuery.updatable(User.class)
                .setColumns(o -> {
                    o.name().set(o.id().toStr());
                    //toStr和.setPropertyType(String.class)效果是一样的
                    o.name().set(o.id().setPropertyType(String.class));
                })
                .where(u -> u.name().eq("王五"))
                .executeRows();
        Assertions.assertTrue(rows > 0);
    }

    @Test
    public void testUpdateIncrement() {
        User user = easyEntityQuery.queryable(User.class).findNotNull(1);
        //自增，可传入指定参数自增
        long rows = easyEntityQuery.updatable(User.class)
                .setColumns(o -> {
                    o.version().increment();
                })
                .where(o -> o.id().eq(user.getId()))
                .executeRows();
        Assertions.assertTrue(rows > 0);
    }

    @Test
    public void testAsTracking() {
        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            trackManager.begin();
            Integer id = 1;
            User existUser = easyEntityQuery.queryable(User.class).asTracking().findNotNull(id);
            existUser.setVersion(existUser.getVersion() + 1);
            easyEntityQuery.updatable(existUser).executeRows();
        } finally {
            trackManager.release();
        }
    }

    @Test
    public void testAddTracking() {
        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            trackManager.begin();
            Integer id = 1;
            User existUser = easyEntityQuery.queryable(User.class).findNotNull(id);
            easyEntityQuery.addTracking(existUser);
            existUser.setVersion(existUser.getVersion() + 1);
            easyEntityQuery.updatable(existUser).executeRows();
        } finally {
            trackManager.release();
        }
    }


    @Test
    public void testUpdateMap() {
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", 1);
        userMap.put("update_time", new Date());
        long rows = easyEntityQuery.mapUpdatable(userMap)
                .asTable("user")
                .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
                .whereColumns("id")
                .executeRows();
        Assertions.assertTrue(rows > 0);
    }

    @Test
    public void testUpdateCustomSQL() {
        long rows = easyEntityQuery.updatable(User.class)
                .setColumns(o -> {
                    o.version().setSQL("ifnull({0},0)+{1}", (context) -> {
                        context.expression(o.version())
                                .value(1);
                    });
                })
                .where(o -> o.id().eq(1))
                .executeRows();
        Assertions.assertTrue(rows > 0);
    }

    @Test
    public void testLogicDelete() {
        Company company = new Company();
        company.setName("新公司");
        company.setDeleted(false);
        easyEntityQuery.insertable(company).executeRows(true);
        long rows = easyEntityQuery.deletable(Company.class)
                .where(c -> c.name().eq("新公司"))
                .executeRows();
        Assertions.assertTrue(rows > 0);

        //根据对象id删除
        company = new Company();
        company.setName("新公司");
        company.setDeleted(false);
        easyEntityQuery.insertable(company).executeRows(true);
        rows = easyEntityQuery.deletable(company).executeRows();
        Assertions.assertTrue(rows > 0);
    }

    @Test
    public void testDelete() {
        Company company = new Company();
        company.setName("新公司");
        easyEntityQuery.insertable(company).executeRows(true);
        long rows = easyEntityQuery.deletable(company)
                .disableLogicDelete()//禁用逻辑删除,使用物理删除 生成delete语句
                .allowDeleteStatement(true)//如果不允许物理删除那么设置允许 配置项delete-throw
                .executeRows();
        Assertions.assertTrue(rows > 0);

        Assertions.assertThrows(EasyQueryInvalidOperationException.class, () -> {
            easyEntityQuery.deletable(company).disableLogicDelete().allowDeleteStatement(false).executeRows();
        });
    }

    @Test
    public void testQueryDisableLogicDelete() {
        //删除所有公司
        easyEntityQuery.deletable(Company.class).where(c -> c.id().isNotNull()).executeRows();
        //查询用户关联未删除的公司
        List<UserVo> userVos = easyEntityQuery.queryable(User.class)
                .leftJoin(Company.class, (u, c) -> u.companyId().eq(c.id()))
                .select(UserVo.class, (u, c) -> Select.of(
                        c.name().as(UserVo::getCompanyName)
                ))
                .toList();
        for (UserVo userVo : userVos) {
            Assertions.assertNull(userVo.getCompanyName());
        }

        //部分禁用逻辑删除，查询用户关联全部公司
        userVos = easyEntityQuery.queryable(User.class)
                .leftJoin(Company.class, (u, c) -> u.companyId().eq(c.id()))
                .tableLogicDelete(() -> false)
                .select(UserVo.class, (u, c) -> Select.of(
                        c.name().as(UserVo::getCompanyName)
                ))
                .toList();
        for (UserVo userVo : userVos) {
            Assertions.assertNotNull(userVo.getCompanyName());
        }
        //查询全部数据，包括已删除的
        List<Company> companyList = easyEntityQuery.queryable(Company.class).disableLogicDelete().toList();
        for (Company company : companyList) {
            company.setDeleted(false);
        }
        //恢复全部数据，包括已删除的
        long size = easyEntityQuery.updatable(companyList).disableLogicDelete().executeRows();
        Assertions.assertEquals(companyList.size(), size);
    }

    @Test
    public void testCustomLogicDelete() {
        Product product = new Product();
        product.setName("香蕉");
        easyEntityQuery.insertable(product).executeRows(true);
        easyEntityQuery.deletable(product).executeRows();
        easyEntityQuery.deletable(product).executeRows();
    }

    @Test
    public void testOnConflictThenUpdate() {
        //根据id字段判断是否存在匹配项，此处存在，更新全部列
        User user = easyEntityQuery.queryable(User.class).findNotNull(1);
        Date updateTime = new Date();
        user.setUpdateTime(updateTime);
        long rows = easyEntityQuery.insertable(user)
                .onConflictThen(o -> o.FETCHER.allFields())
                .executeRows();
        Assertions.assertTrue(rows > 0);

        //根据id字段判断是否存在匹配项，此处存在，更新指定列
        user = easyEntityQuery.queryable(User.class).findNotNull(1);
        updateTime = new Date();
        user.setUpdateTime(updateTime);
        rows = easyEntityQuery.insertable(user)
                .onConflictThen(o -> o.FETCHER.updateTime())
                .executeRows();
        Assertions.assertTrue(rows > 0);
    }

    @Test
    public void testOnConflictThenInsert() {
        //根据id字段判断是否存在匹配项，此处不存在，插入全部列
        User user = new User();
        Date createTime = new Date();
        user.setName("新用户");
        user.setCreateTime(createTime);
        user.setVersion(1);
        user.setEnabled(true);
        long rows = easyEntityQuery.insertable(user)
                //mysql不支持使用多列进行判断是否存在匹配项
                .onConflictThen(null, o -> o.FETCHER.id())
                .executeRows();
        Assertions.assertTrue(rows > 0);
    }

    @Test
    public void testTransaction() {
        try (Transaction transaction = easyEntityQuery.beginTransaction()) {
            User user = new User();
            user.setName("新用户");
            user.setVersion(1);
            user.setEnabled(true);
            easyEntityQuery.insertable(user).executeRows();
            easyEntityQuery.insertable(user).executeRows();
            if (true) {
                throw new RuntimeException("模拟异常");
            }
            transaction.commit();
        }
    }

    @Test
    public void testApiMode() {
        DataSource dataSource = Config.getDataSource();
        //采用控制台输出打印sql
        LogFactory.useStdOutLogging();
        //属性模式
        EasyQueryClient easyQueryClient = EasyQueryBootstrapper.defaultBuilderConfiguration()
                .setDefaultDataSource(dataSource)
                .optionConfigure(op -> {
                    op.setPrintSql(true);
                    op.setKeepNativeStyle(true);
                    op.setDefaultTrack(true);
                })
                .useDatabaseConfigure(new H2DatabaseConfiguration())
                .build();
        User user = easyQueryClient.queryable(User.class)
                .where(u -> u.eq("name", "张三"))
                .firstNotNull();
        user.setUpdateTime(new Date());
        easyQueryClient.updatable(user).executeRows();
        Integer userId = user.getId();
        easyQueryClient.updatable(User.class)
                .set("updateTime", new Date())
                .where(o -> o.eq("id", userId))
                .executeRows();

        //lambda模式
        DefaultEasyQuery easyQuery = new DefaultEasyQuery(easyQueryClient);
        user = easyQuery.queryable(User.class)
                .where(u -> u.eq(User::getName, "张三"))
                .firstNotNull();
        user.setUpdateTime(new Date());
        easyQuery.updatable(user).executeRows();
        easyQuery.updatable(User.class)
                .set(User::getUpdateTime, new Date())
                .where(o -> o.eq(User::getId, userId))
                .executeRows();

        //代理模式
        DefaultEasyProxyQuery easyProxyQuery = new DefaultEasyProxyQuery(easyQueryClient);
        user = easyProxyQuery.queryable(UserProxy.createTable())
                .where(u -> u.name().eq("张三"))
                .firstNotNull();
        user.setUpdateTime(new Date());
        easyProxyQuery.updatable(user).useProxy(UserProxy.createTable()).executeRows();
        easyProxyQuery.updatable(UserProxy.createTable())
                .setColumns(o -> {
                    o.updateTime().set(new Date());
                })
                .where(o -> o.id().eq(userId))
                .executeRows();

        //对象模式
        DefaultEasyEntityQuery easyEntityQuery = new DefaultEasyEntityQuery(easyQueryClient);
        easyEntityQuery.updatable(user).executeRows();
        easyEntityQuery.updatable(User.class)
                .setColumns(o -> {
                    o.updateTime().set(new Date());
                })
                .where(o -> o.id().eq(userId))
                .executeRows();
    }
}

