package edu.zju.bme.hibernarm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.internal.util.ReflectHelper;
import org.junit.Test;
import org.openehr.am.archetype.Archetype;
import org.openehr.rm.binding.DADLBinding;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.util.GenerationStrategy;
import org.openehr.rm.util.SkeletonGenerator;

import se.acode.openehr.parser.ADLParser;

public class AQLExecutePerformanceTest extends AQLExecuteTestBase {

	public AQLExecutePerformanceTest() throws IOException {
		super();
	}

	@Test
	public void testInsertSelect1000() throws Exception {
		cleanTestBaseData();

		List<String> dadls = new ArrayList<String>();
		DADLBinding binding = new DADLBinding();
		SkeletonGenerator generator = SkeletonGenerator.getInstance();
		String archetypeString = archetypes.get("openEHR-DEMOGRAPHIC-PERSON.patient.v1");
		ADLParser parser = new ADLParser(archetypeString);
		Archetype archetype = parser.parse();
		for (int i = 0; i < 1000; i++) {
			HashMap<String, Object> values = new HashMap<String, Object>();
			values.put("/uid/value", UUID.randomUUID().toString());
			values.put("/details[at0001]/items[at0003]/value/value", "M");
			values.put("/details[at0001]/items[at0004]/value/value", "1984-08-11T19:20:30+08:00");
			values.put("/details[at0001]/items[at0009]/value/value", "zhangsan");

			Object result = generator.create(archetype, GenerationStrategy.MAXIMUM_EMPTY);
			if (result instanceof Locatable) {
				Locatable loc = (Locatable) result;
				ReflectHelper.setArchetypeValues(loc, values, archetype);
				dadls.add(binding.toDADLString(loc));
			}
		}
		
		System.out.println("insert dadl count : " + dadls.size());

		long start = System.currentTimeMillis();
		aqlImpl.insert(dadls);
		long end = System.currentTimeMillis();
		System.out.println("insert dadl time : " + (end - start));

		String query = "from openEHR-DEMOGRAPHIC-PERSON.patient.v1 as o ";
		start = System.currentTimeMillis();
		List<?> l = aqlImpl.select(query);
		end = System.currentTimeMillis();
		System.out.println("select dadl count : " + l.size());
		System.out.println("select dadl time : " + (end - start));
	}

}
