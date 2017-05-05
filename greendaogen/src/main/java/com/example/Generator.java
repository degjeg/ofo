package com.example;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class Generator {
    public static final String BEEN_PACKAGE = "o.f.o.com.shareofo.db.model";
    public static final String DAO_PACKAGE = "o.f.o.com.shareofo.db.dao";
    public static final int DB_VERSION = 1;

    static class Cfg {
        public boolean genBean;

        public Cfg(boolean genBean) {
            this.genBean = genBean;
        }
    }

    public static void main(String[] args) throws Exception {

        Cfg cfg;

        cfg = new Cfg(true);
        gen(cfg, "app/src/main/java");

        // cfg = new Cfg(true);
        // gen(cfg, "greendaogen/java-gen");
    }

    public static void gen(Cfg cfg, String outDir) throws Exception {

        // 正如你所见的，你创建了一个用于添加实体（Entity）的模式（Schema）对象。
        // 两个参数分别代表：数据库版本号与自动生成代码的包路径。
        // Schema schema = new Schema(1, "me.itangqi.greendao");
        // 当然，如果你愿意，你也可以分别指定生成的 Bean 与 DAO 类所在的目录，只要如下所示：


        if (outDir.startsWith("app") && cfg.genBean) {
            // by danger 请手写实体类!
            // throw new Exception("请手写实体类!");
        }
        Schema schema = new Schema(DB_VERSION, BEEN_PACKAGE);
        schema.setDefaultJavaPackageDao(DAO_PACKAGE);

        // 模式（Schema）同时也拥有两个默认的 flags，分别用来标示 entity 是否是 activie 以及是否使用 keep sections。
        schema.enableActiveEntitiesByDefault();
        schema.enableKeepSectionsByDefault(); // 通过次Schema对象添加的所有实体都不会覆盖自定义的代码


        // 一旦你拥有了一个 Schema 对象后，你便可以使用它添加实体（Entities）了。
        addOfoData(schema, cfg);


        // 测试工作目录位置
        File f = new File("java-gen");
        System.out.println("" + f.getAbsolutePath());
        // 最后我们将使用 DAOGenerator 类的 generateAll() 方法自动生成代码，此处你需要根据自己的情况更改输出目录（既之前创建的 java-gen)。
        // 其实，输出目录的路径可以在 build.gradle 中设置，有兴趣的朋友可以自行搜索，这里就不再详解。
        new DaoGenerator().generateAll(schema, outDir);
    }

    private static void initCfg(Entity entity, Cfg cfg) {
        if (!cfg.genBean) {
            entity.setSkipGeneration(true);
        }
    }

    private static void addOfoData(Schema schema, Cfg cfg) {
        Entity messageEntity = schema.addEntity("BicycleData");

        messageEntity.setJavaPackage(BEEN_PACKAGE);
        messageEntity.setClassNameDao("BicycleDao");
        messageEntity.setTableName("bicycle");

        messageEntity.addIdProperty();
        messageEntity.addStringProperty("code");
        messageEntity.addStringProperty("password");
        messageEntity.addDateProperty("time");
        messageEntity.addBooleanProperty("deleted");
        messageEntity.addStringProperty("desc");

        messageEntity.implementsSerializable();

        //
        initCfg(messageEntity, cfg);
    }


}
