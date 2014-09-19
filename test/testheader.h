#include <file1.h>
 # include "File1.h"
/* comment */ /*
#include <comment1.h>
*/

/*comment*/ #include <notcomment.h>

/*
#include <comment2.h>
*/

/* #include <comment3.h> */

//#include "comment4.h"

            #              include      <stuff.h>         

			
//The following is just a bunch of stuff to make the file bigger
extern class CDIIDAllocator *g_cdiidalloc;

#define ALT_DATA_SIZE 24  //Used for storing integers as strings

#ifdef _CDI_TEST_
extern void nodeIncrement();
extern void nodeDecrement();

#endif

extern s_sintn LockModule(bool lockf);

CDINodeImpl::CDINodeImpl()
	{
	nerr_t err = 0;
	
#ifdef _CDI_TEST_
	gInit.throwException();
	nodeIncrement();
#endif
	m_refCount = 0;
	m_objectPool = 0;
	if ((err = init(0, 0, 0)) != 0)
		throw (err);
	}

CDINodeImpl::CDINodeImpl(CDIDocumentImpl* doc, CDINodeImpl *parent, CDINodeData *nodeData)
	{
	nerr_t err = 0;
	
#ifdef _CDI_TEST_
	gInit.throwException();
	nodeIncrement();
#endif
	m_refCount = 0;
	m_objectPool = 0;
	
	if ((err = init(doc, parent, nodeData)) != 0)
		throw (err);
	}

nerr_t CDINodeImpl::init(CDIDocumentImpl* doc, CDINodeImpl *parent, CDINodeData *nodeData)
	{
	dynamicMemberInit();
	m_CDIDocument = doc;
	
	m_nodeData = nodeData;
	if (m_nodeData != 0)
		m_nodeData->AddRef();
	
	m_firstAttribute = 0;
	m_firstChild = 0;
	m_nextSibling = 0;
	m_parent = parent;
	m_nodeID = 0;
#ifdef _DEBUG
	m_nodeName = NULL;
#endif
	m_prevSibling = 0;	
	m_flags = 0;

	if (nodeData != 0)
		nodeData->setNode(this);
	
	return (0);
	}

CDINodeImpl::~CDINodeImpl()
	{
#ifdef _CDI_TEST_
	nodeDecrement();
#endif
	//printf("delete CDINodeImpl\n");
	reset();

	if (m_objectPool != NULL)
		m_objectPool->Release();
	}

void CDINodeImpl::reset()
	{
	void *tmp;

	if (m_nodeData != 0)
		{
		if (checkFlag(CDINF_SENSITIVE_DATA))
			m_nodeData->setNodeDataFlags(CDINDF_SENSITIVE_DATA);
		m_nodeData->Release();
		m_nodeData = 0;
		}
	
	if ((tmp = getDynamicMember(CDIDT_ALTDATABUF)) != 0)
		gInit.free(tmp);
		
	if ((tmp = getDynamicMember(CDIDT_LOCALNAME)) != 0)
		gInit.free(tmp);

	if ((tmp = getDynamicMember(CDIDT_NAMESPACE)) != 0)
		gInit.free(tmp);
		
	dynamicMemberDestroy();
	}

void CDINodeImpl::returnToPool()
	{
	s_sintn ref;
	ObjectPool *op;

	ref = m_refCount;
	op = m_objectPool;
	
	reset();

	//init(0, 0, 0, 0);
	m_refCount = ref;
	m_objectPool = op;
	}
	
	
CDINodeImpl* CDINodeImpl::duplicateNode()
	{
	CDINodeImpl* newNode = 0;
	
	m_CDIDocument->getCDINodeImpl(&newNode, m_parent, m_nodeData);
	if (newNode == 0) goto Exit;
	
	newNode->m_prevSibling = m_prevSibling;
	newNode->m_flags = m_flags;
	newNode->setFlags(CDINF_DUPLICATED_NODE);
	newNode->setFlags(CDINF_SHARED_DATA);
	setFlags(CDINF_SHARED_DATA);
	newNode->m_nodeID = m_nodeID;
#ifdef _DEBUG
	newNode->m_nodeName = m_nodeName;
#endif
	
	
	getNextSibling((CDINode**)&(newNode->m_nextSibling));
	getFirstChild((CDINode**)&(newNode->m_firstChild));
	getFirstAttribute((CDINode**)&(newNode->m_firstAttribute));
	
Exit:
	return (newNode);
	}

//-------------------------------------------------------------------
void CDINodeImpl::duplicateNode(CDINodeImpl *dup)
	{
	dup->m_flags = m_flags;
	dup->m_nodeData = m_nodeData;
	dup->m_nodeData->AddRef();
	dup->m_nodeID = m_nodeID;
#ifdef _DEBUG
	dup->m_nodeName = m_nodeName;
#endif
	dup->setFlags(CDINF_DUPLICATED_NODE);
	dup->setFlags(CDINF_SHARED_DATA);
	setFlags(CDINF_SHARED_DATA);
	}
	
void CDINodeImpl::duplicateFlags(CDINodeImpl *dup)
	{
	dup->m_flags = m_flags;
	}
	
	
//-------------------------------------------------------------------
void CDINodeImpl::inheritNamespaces(CDINodeImpl *ben)
	{
	nerr_t err = 0;
	CDINodeImpl *parent;
	CDINodeList *attrs;
	CDINode *att, *newAtt;
	const char *loc;
	CDI_ID name;
	const char *val;
	size_t len;
	
	parent = m_parent;
	//_asm{int 3};
	while (parent != 0)
		{
		err = parent->getAttributes(&attrs);
		if (err == 0)
			{
			while(attrs->getNextNode(&att) == 0)
				{
				att->getNodeLocalName(&loc);
				if (strncmp(loc, "xmlns", 5) == 0)
					{
					err = att->getNodeNameAsID(&name);
					if (err != 0) continue;
					err = att->getValueAsString(&val, &len);
					if (err != 0) continue;
					err = ben->setAttribute(&newAtt, name);
					if (err != 0) continue;
					newAtt->setValueAsString((char*)val, len);
					}
				}
			}
		
		parent = parent->m_parent;
		}
	}

//void CDINodeImpl::set(CDIDocumentImpl* doc, CDIDocumentData* documentData, CDINodeImpl *parent, CDINodeData *nodeData)
//	{
//	nerr_t err = 0;
//
//	m_documentData = 0;
//	if (nodeData != 0)
//		err = nodeData->getDocumentData(&m_documentData);
//
//	if (m_documentData == 0)
//		if ((parent != 0)&&(parent->m_documentData != 0))
//			m_documentData = parent->m_documentData;
//		else
//			m_documentData = documentData;
//
//	m_CDIDocument = doc;
//	m_nodeData = nodeData;
//	m_parent = parent;
//
//	if (m_nodeData != 0)
//		nodeData->setNode(this);
//	
//	}

//============== PCIUnknown  ====================================
int PCOMAPI CDINodeImpl::QueryInterface(RPCIID riid, void** ppv)
	{
	return NE_SAL_INVALID_PARAMETER;
	}

//-------------------------------------------------------------
s_uint32 PCOMAPI CDINodeImpl::AddRef(void)
	{
	s_uintn ref = SAL_AtomicIncrement(&m_refCount);
	if (ref == 1)
		LockModule(true);
		
	return ((s_uint32)ref);
	}

//-------------------------------------------------------------
s_uint32 PCOMAPI CDINodeImpl::Release(void)
	{
	s_sintn ref;
	
	ref = SAL_AtomicDecrement(&m_refCount);
	s_assert(ref >= 0);
	if ((ref == 1)&&(m_objectPool != 0))
		{
		returnToPool();
		m_objectPool->returnToPool((PooledObject*)this);		
		}

	if (ref == 0)
		{
		delete this;
		LockModule(false);
		}
		
	return ((s_uint32)ref);
	}

//============== PooledObject   ====================================
void CDINodeImpl::setObjectPool(ObjectPool *op)
	{
	m_objectPool = op;
	m_objectPool->AddRef();
	} 

//============== CDINodeImpl   ====================================
CDINodeImpl* CDINodeImpl::getAlternateNode()
	{
	CDINodeImpl* alt = 0;
	
	alt = (CDINodeImpl*)getDynamicMember(CDIDT_ALTNODE);
	return (alt);
	}
	

//-------------------------------------------------------------------
void CDINodeImpl::appendNode(CDINodeImpl *branch, CDINodeImpl *node)
	{
	CDINodeImpl *n = branch;
	CDINodeImpl *last = n;

	node->setM_Parent(branch->m_parent);
	while((n->getNextSibling((CDINode**)&n)) == 0)
		{
		last = n;
		}
	last->setM_NextSibling(node);
	node->setM_PrevSibling(last);

	last->clearFlags(CDINF_LAST_SIBLING);
	node->setFlags(CDINF_LAST_SIBLING);
	}

//============== CDINodeImpl   ====================================
CDINodeData* CDINodeImpl::getNodeData()
	{
	return (m_nodeData);
	}

//-------------------------------------------------------------
void CDINodeImpl::setNodeData(CDINodeData* nodeData)
	{
	m_nodeData = nodeData;
	m_nodeData->setNode(this);
	return;
	}


//-------------------------------------------------------------
CDI_NODE_TYPE CDINodeImpl::getNodeType()
	{
	if (m_flags & CDINF_ELEMENT)
		return (CDI_ELEMENT);
	else if (m_flags & CDINF_ATTRIBUTE)
		return (CDI_ATTRIBUTE);
	else
		return (CDI_UNDEFINED);
	}

//-------------------------------------------------------------
void CDINodeImpl::setNodeType(CDI_NODE_TYPE type)
	{
	clearFlags(CDINF_ELEMENT);
	clearFlags(CDINF_ATTRIBUTE);

	if (type == CDI_ELEMENT)
		setFlags(CDINF_ELEMENT);
	else if (type == CDI_ATTRIBUTE)
		setFlags(CDINF_ATTRIBUTE);

	}

//-------------------------------------------------------------
nerr_t CDINodeImpl::getParentNode(CDINode **parent)
	{
	*parent = m_parent;
	if (m_parent != 0)
		return (0);
	else
		return (NE_CDI_OBJECT_NOT_FOUND);
	}

//-------------------------------------------------------------
nerr_t CDINodeImpl::getPreviousSibling(CDINode **sibling)
	{
	*sibling = m_prevSibling;
	if (m_prevSibling != 0)
		return (0);
	else
		return (NE_CDI_OBJECT_NOT_FOUND);
	}

//-------------------------------------------------------------


//-------------------------------------------------------------
CDI_VALUE_TYPE CDINodeImpl::getValueType()
	{
	if (m_nodeData != 0)
		return(m_nodeData->getValueType());

	return (CDIT_UNDEFINED);
	}


//===================== New CDINode API's ===========================
nerr_t CDINodeImpl::getChildNodes(CDINodeList **nodeList)
	{
	nerr_t err = 0;
	CDINodeListImpl *nl;
	CDINode *n;

	*nodeList = 0;	
	err = getFirstChild(&n);
	if (err != 0)
		goto Exit;

	nl = m_CDIDocument->getCDINodeListImpl((CDINodeImpl*)n, 0);
	if (nl == 0)
		{
		err = NE_CDI_INSUFFICIENT_MEMORY;
		}
	*nodeList = (CDINodeList*)nl;

Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getFirstChild(CDINode **node)
	{
	nerr_t err = 0;
	CDINodeData* nd = 0;
	CDIDocumentData *docData = 0;

	*node = 0;
	if (m_flags & CDINF_ATTRIBUTE)
		{
		return (NE_CDI_INVALID_REQUEST);		
		}

	if (m_firstChild == 0)
		{
		m_CDIDocument->lockDocument();
		m_nodeData->getDocumentData(&docData);
		
		if ((m_firstChild == 0)&&(docData != 0)&&(!(m_flags & CDINF_NO_CHILDREN)))
			{
			err = docData->getFirstChildNodeData(&nd, this->getNodeData());
			if ((err != 0) || (nd == NULL))
				{
				if (IsBaseErr(err, NE_NOT_IMPLEMENTED) || IsBaseErr(err, NE_OBJECT_NOT_FOUND))
					err = 0;	//No children

				goto Exit;
				}
		
			err = m_CDIDocument->getCDINodeImpl(&m_firstChild, this, nd);
			if (err != 0) 
				goto Exit;
	
			m_firstChild->setNodeType(CDI_ELEMENT);
			}
		
Exit:
		if (nd != 0)
			nd->Release();
			
		if (docData != NULL)
			docData->Release();
			
		if ((m_firstChild == 0)&&(err == 0))
			{
			err = m_CDIDocument->getEmbeddedNode((CDINode**)&m_firstChild, this);
			if (m_firstChild != 0)
				m_firstChild->setM_Parent(this);
			}
			
		m_CDIDocument->unlockDocument();
		}

	if (err == 0)
		{
		if (m_firstChild != NULL)
			{
			if (m_firstChild->isHidden())
				err = m_firstChild->getNextSibling(node);
			else
				*node = (CDINode*)m_firstChild;
			}
		else
			{
			setFlags(CDINF_NO_CHILDREN);
			err = NE_CDI_OBJECT_NOT_FOUND;
			}
		}

#ifdef SAL_DEBUG
	if (m_firstChild != 0)
		{
		if (!(m_flags & CDINF_DUPLICATED_NODE)&&(m_firstChild->m_parent != this))
			SAL_DebugAssertFailed("Detached child pointer.", __FILE__, __LINE__);
		}
#endif
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getNodeNameAsID(CDI_ID *id)
	{
	nerr_t err = 0;

	*id = 0;
	if ((m_nodeID == 0)&&(m_nodeData != 0)&&(m_nodeData->getNodeID() != 0))
		{
		m_nodeID = m_nodeData->getNodeID();
#ifdef _DEBUG
		size_t len;
		const char *ns;
		
		g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &ns, &len, &m_nodeName, &len);
#endif
		}

	*id = m_nodeID;
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::setNodeNameAsID(CDI_ID id)
	{
	nerr_t err = 0;

	if (id == 0)
		err = NE_CDI_INVALID_PARAMETER;
	else
		{
		m_nodeID = id;
#ifdef _DEBUG
		size_t len;
		const char *ns;
		
		g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &ns, &len, &m_nodeName, &len);
#endif
		}
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::setNodeName(const char *nameSpace, const char *locName)
	{
	nerr_t err = 0;

	if (locName == 0)
		{
		err = NE_CDI_INVALID_PARAMETER;
		goto Exit;
		}

	m_nodeID = g_cdiidalloc->getIDFromName(nameSpace, locName);

#ifdef _DEBUG
		size_t len;
		const char *ns;
		
		g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &ns, &len, &m_nodeName, &len);
#endif

	if (m_nodeID == 0)
		err = NE_CDI_INSUFFICIENT_MEMORY;
Exit:
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getValueAsID(CDI_ID *id)
	{
	nerr_t err = 0;
	const char *val = 0, *tag = 0, *ns = 0, *loc = 0;
	size_t valLen = 0, tagLen = 0, nsLen = 0, locLen = 0;
	size_t I;
	
	*id = 0;

	if (m_nodeData != 0)
		{
		err = m_nodeData->getValueAsID(id);
		if (err != 0)
			{
			err = this->getValueAsString(&val, &valLen);
			if (err != 0) goto Exit;

			if (valLen == 0)
				{
				err = NE_CDI_EMPTY_NODE;
				goto Exit;
				}

			for (I = 0; I < valLen; I++)
				{
				if (val[I] == ':')
					{
					tag = val;
					tagLen = I;
					loc = &val[I + 1];
					locLen = valLen - (I+1);
					break;
					}
				}

			if (tagLen == 0)
				*id = g_cdiidalloc->getIDFromNameWithLen("", 0, val, valLen);
			else
				{
				err = m_CDIDocument->getNameSpaceFromTag((CDINode*)this, &ns, &nsLen, tag, tagLen);
				if (err == 0)
					*id = g_cdiidalloc->getIDFromNameWithLen(ns, nsLen, loc, locLen);
				else
					*id = g_cdiidalloc->getIDFromNameWithLen("", 0, val, valLen);
				}
			}
		}

Exit:
	return (err);	
	}


//-------------------------------------------------------------------
nerr_t CDINodeImpl::setValueAsID(CDI_ID id)
	{
	nerr_t err = 0;
	const char *ns, *loc, *tag;
	char *buf;
	size_t nsLen, locLen, tagLen, bufLen;

	if (id == 0)
		{
		err = NE_CDI_INVALID_PARAMETER;
		goto Exit;
		}
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->setValueAsID(id);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			//Try to set the value as a string
			g_cdiidalloc->getNameFromIDAsPtr(id, &ns, &nsLen, &loc, &locLen);
			m_CDIDocument->getTagFromNameSpace((CDINode*)this, &tag, &tagLen, ns, nsLen);
			printf("tagLen = %d\n", tagLen);
			if (tagLen != 0)
				{
				bufLen = tagLen + locLen + 2;  //Add extra for ':' and a null at the end
				buf = (char*)gInit.malloc(bufLen);
				if (buf == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				strncpy(buf, tag, tagLen);
				buf[tagLen] = ':';
				buf[tagLen+1] = '\0';
				strncat(buf, loc, locLen);
				err = m_nodeData->setValueAsString(buf, (bufLen-1));
				gInit.free((void*)buf);
				}
			else
				{
				err = m_nodeData->setValueAsString(loc, locLen);
				}
			}
		}
	
	
Exit:
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (err);
	}


//-------------------------------------------------------------------
nerr_t CDINodeImpl::setAttributeByName(CDINode **att, const char *nameSpace, const char *locName)
	{
	nerr_t err = 0;
	CDI_ID id;

	*att = 0;
	id = g_cdiidalloc->getIDFromName(nameSpace, locName);
	err = setAttribute(att, id);

	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getNodeNamespace(const char **nsPtr)
	{
	nerr_t err = 0;
	size_t nslen, llen;
	const char *ns, *l;
	char *tmp;

	*nsPtr = (const char*)getDynamicMember(CDIDT_NAMESPACE);
	if (*nsPtr == 0)
		{
		if ((m_nodeID == 0)&&(m_nodeData != 0))
			m_nodeID = m_nodeData->getNodeID();

		g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &ns, &nslen, &l, &llen);
		tmp = (char*)gInit.malloc(nslen+1);
		if (tmp == 0)
			{
			err = NE_CDI_INSUFFICIENT_MEMORY;
			goto Exit;
			}	

		err = setDynamicMember(CDIDT_NAMESPACE, (void*)tmp);
		if (err != 0)
			{
			gInit.free(tmp);
			}
		strncpy(tmp, ns, nslen);
		tmp[nslen] = '\0';
		*nsPtr = tmp;
		//g_cdiidalloc->getNameFromID(m_nodeID, (char *const)m_namespace, CDI_NAMESPACE_SIZE, (char *const)m_localName, CDI_LOCALNAME_SIZE);

#ifdef _DEBUG
		size_t lentemp;
		const char *nstemp;
		
		g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &nstemp, &lentemp, &m_nodeName, &lentemp);
#endif
		}

Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getNodeLocalName(const char **lPtr)
	{
	nerr_t err = 0;
	size_t nslen, llen;
	const char *ns, *l;
	char* tmp;

	*lPtr = (const char*)getDynamicMember(CDIDT_LOCALNAME);
	if (*lPtr == 0)
		{
		if ((m_nodeID == 0)&&(m_nodeData != 0))
			m_nodeID = m_nodeData->getNodeID();

		g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &ns, &nslen, &l, &llen);
		tmp = (char*)gInit.malloc(llen+1);
		if (tmp == 0)
			{
			err = NE_CDI_INSUFFICIENT_MEMORY;
			goto Exit;
			}

		err = setDynamicMember(CDIDT_LOCALNAME, (void*)tmp);
		if (err != 0)
			{
			gInit.free(tmp);
			}
		strncpy(tmp, l, llen);
		tmp[llen] = '\0';
		*lPtr = tmp;
		//g_cdiidalloc->getNameFromID(m_nodeID, (char *const)m_namespace, CDI_NAMESPACE_SIZE, (char *const)m_localName, CDI_LOCALNAME_SIZE);
#ifdef _DEBUG
		size_t lentemp;
		const char *nstemp;
		
		g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &nstemp, &lentemp, &m_nodeName, &lentemp);
#endif
		}

Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getNextSibling(CDINode **sibling)
	{
	nerr_t err = 0;
	CDINodeData* nd = NULL;
	CDIDocumentData *docData = NULL;

	*sibling = 0;

	if (m_nextSibling == NULL)
		{
		m_CDIDocument->lockDocument();
		if (m_parent != NULL)
			docData = m_parent->getCDIDocumentData();
		
		if ((m_nextSibling == NULL)&&(docData != 0)&&(!(m_flags & CDINF_LAST_SIBLING)))
			{
			err = docData->getNextNodeData(&nd, this->getNodeData());
			if ((err != 0) || (nd == NULL))
				{
				if (IsBaseErr(err, NE_NOT_IMPLEMENTED) || IsBaseErr(err, NE_OBJECT_NOT_FOUND))
					err = 0;	//No more siblings

				goto Exit;
				}
				
			err = m_CDIDocument->getCDINodeImpl(&m_nextSibling, m_parent, nd);
			if (err != 0) 
				goto Exit;
			
			m_nextSibling->setNodeType(getNodeType());
			}
Exit:
		if (nd != 0)
			nd->Release();
			
		if (docData != NULL)
			docData->Release();
			
		if ((m_nextSibling == 0)&&(!(m_flags & CDINF_ATTRIBUTE))&&(m_parent != 0)&&(err == 0))
			{
			//This call is made to the parent nodes CDIDocument so if it is embedded
			//The right document is querried for the node
			err = m_parent->m_CDIDocument->getEmbeddedNode((CDINode**)&m_nextSibling, m_parent);
			
			if (m_nextSibling != 0)
				m_nextSibling->setM_Parent(m_parent);
			}

		m_CDIDocument->unlockDocument();
		}

	if (err == 0)
		{
		if (m_nextSibling != NULL)
			{
			if (m_nextSibling->isHidden())
				{
				err = m_nextSibling->getNextSibling(sibling);
				}
			else
				{
				*sibling = (CDINode*)m_nextSibling;
				m_nextSibling->setM_PrevSibling(this);
				}
			}
		else
			{
			setFlags(CDINF_LAST_SIBLING);
			err = NE_CDI_OBJECT_NOT_FOUND;
			}
		}

#ifdef SAL_DEBUG
	if (m_nextSibling != 0)
		{
		if (m_nextSibling->m_prevSibling != this)
			SAL_DebugAssertFailed("Detached sibling pointer.", __FILE__, __LINE__);
		}
#endif

	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttributes(CDINodeList **attList)
	{
	nerr_t err = 0;
	CDINode *n;
	CDINodeListImpl *nl;

	*attList = 0;
	err = getFirstAttribute(&n);
	if (err != 0)
		goto Exit;

	nl = m_CDIDocument->getCDINodeListImpl((CDINodeImpl*)n, 0);
	if (nl == 0)
		{
		err = NE_CDI_INSUFFICIENT_MEMORY;
		}
	*attList = nl;

Exit:
	return (err);
	}


//-------------------------------------------------------------------
nerr_t CDINodeImpl::getFirstAttribute(CDINode **attNode)
	{
	nerr_t err = 0;
	CDINodeData* nd = 0;
	CDIDocumentData *docData = NULL;

	*attNode = 0;
	if (m_flags & CDINF_ATTRIBUTE)
		return(NE_CDI_INVALID_REQUEST);

	if ((m_firstAttribute == NULL)&&(!(m_flags & CDINF_NO_ATTRIBUTES)))
		{
		m_CDIDocument->lockDocument();
		m_nodeData->getDocumentData(&docData);
		
		if ((m_firstAttribute == 0)&&(docData != 0)&&(!(m_flags & CDINF_NO_ATTRIBUTES)))
			{
			err = docData->getFirstAttributeNodeData(&nd, this->getNodeData());
			if ((err != 0) || (nd == NULL))
				{
				if (IsBaseErr(err, NE_NOT_IMPLEMENTED) || IsBaseErr(err, NE_OBJECT_NOT_FOUND))
					err = 0;	//No more siblings

				goto Exit;
				}
				
			err = m_CDIDocument->getCDINodeImpl(&m_firstAttribute, this, nd);
			if (err != 0) 
				goto Exit;
			
			m_firstAttribute->setNodeType(CDI_ATTRIBUTE);
			}
		
Exit:
		if (nd != NULL)
			nd->Release();
			
		if (docData != NULL)
			docData->Release();

		m_CDIDocument->unlockDocument();
		}

	if (err == 0)
		{
		if (m_firstAttribute != 0)
			{
			if (m_firstAttribute->isHidden())
				{
				err = m_firstAttribute->getNextSibling(attNode);
				}
			else
				{
				*attNode = m_firstAttribute;
				}
			}
		else
			{
			setFlags(CDINF_NO_ATTRIBUTES);
			err = NE_CDI_OBJECT_NOT_FOUND;
			}
		}

		
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttribute(CDINode **attNode, CDI_ID attID)
	{
	nerr_t err = 0;
	CDINode *n = 0;
	CDI_ID id;

	*attNode = 0;
	if (attID == 0)
		{
		err = NE_CDI_INVALID_PARAMETER;
		goto Exit;
		}
	err = getFirstAttribute(&n);
	if (err != 0)
		goto Exit;

	while ((n != 0)&&(err == 0))
		{
		n->getNodeNameAsID(&id);
		if (id == attID)
			break;
		err = n->getNextSibling(&n);
		}
	
	if (err == 0)
		*attNode = n;

Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::setAttribute(CDINode **attNode, CDI_ID attID)
	{
	nerr_t err = 0;
	CDINodeData *nd = 0;
	CDINodeImpl *node = 0;
	CDINode *n = 0;
	bool nodeDataImpl = false;
	CDIDocumentData *docData = NULL;

	*attNode = 0;
	
	if (attID == 0)
		{
		err = NE_CDI_INVALID_NODE_ID;
		goto Exit;
		}
		
	if (m_flags & CDINF_ATTRIBUTE)
		{
		return (NE_CDI_INVALID_REQUEST);		
		}
	
	if (getAttribute(&n, attID) == 0)
		{
		*attNode = n;
		goto Exit;
		}

	m_CDIDocument->lockDocument();
	m_nodeData->getDocumentData(&docData);
	
	if (docData != NULL)
		{
		err = docData->getAttributeNodeDataByID(&nd, m_nodeData, attID);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED) || IsBaseErr(err, NE_OBJECT_NOT_FOUND)||(nd == 0))
			err = 0;
		else
			goto UnlockExit;
		}
	if (nd == 0)
		{
		nd = (CDINodeData*)m_CDIDocument->getCDINodeDataImpl();
		if (nd == 0)
			{
			err = NE_CDI_INSUFFICIENT_MEMORY;
			goto UnlockExit;
			}
		nodeDataImpl = true;
		}
	
	err = m_CDIDocument->getCDINodeImpl(&node, this, nd);
	if (err != 0) goto UnlockExit;

	node->setNodeNameAsID(attID);
	node->setNodeType(CDI_ATTRIBUTE);
	if (m_firstAttribute == 0)
		{
		m_firstAttribute = node;
		m_firstAttribute->setFlags(CDINF_LAST_SIBLING);
		}
	else
		appendNode(m_firstAttribute, node);

	*attNode = node;
	
UnlockExit:
	m_CDIDocument->unlockDocument();
	
Exit:
	if (nd != 0)
		nd->Release();
		
	if (docData != NULL)
		docData->Release();
		
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::setAttributeWithData(CDINode **attNode, CDINodeData *nodeData)
	{
	nerr_t err = 0;
	CDINodeImpl *node;

	*attNode = 0;
	if (nodeData == 0)
		{
		err = NE_CDI_INVALID_PARAMETER;
		goto Exit;
		}
		
	if (m_flags & CDINF_ATTRIBUTE)
		{
		return (NE_CDI_INVALID_REQUEST);		
		}

	err = m_CDIDocument->getCDINodeImpl(&node, this, nodeData);
	if (err != 0) goto Exit;

	node->setNodeType(CDI_ATTRIBUTE);
	if (m_firstAttribute == 0)
		m_firstAttribute = node;
	else
		appendNode(m_firstAttribute, node);

	*attNode = node;

Exit:
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getElementsByID(CDINodeList **nodeList, CDI_ID elementID)
	{
	nerr_t err = 0;
	CDINodeListImpl *nl;
	CDINode *n;
	CDI_ID id;

	*nodeList = 0;
	if (elementID == 0)
		{
		err = NE_CDI_INVALID_PARAMETER;
		goto Exit;
		}

	err = getFirstChild(&n);
	if (err != 0)
		goto Exit;

	while (n != 0)
		{
		n->getNodeNameAsID(&id);
		if (id == elementID)
			break;
		err = n->getNextSibling(&n);
		}

	if (err != 0)
		goto Exit;

	nl = m_CDIDocument->getCDINodeListImpl((CDINodeImpl*)n, elementID);
	if (nl == 0)
		err = NE_CDI_INSUFFICIENT_MEMORY;
	else
		*nodeList = nl;

Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getElementsByName(CDINodeList **nodeList, const char *nameSpace, const char *locName)
	{
	CDI_ID id;

	id = g_cdiidalloc->getIDFromName(nameSpace, locName);

	return (getElementsByID(nodeList, id));
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getElementsByNameWithLen(CDINodeList **nodeList, const char *nameSpace, size_t nsLen, const char *locName, size_t locLen)
	{
	CDI_ID id;

	id = g_cdiidalloc->getIDFromNameWithLen(nameSpace, nsLen, locName, locLen);

	return (getElementsByID(nodeList, id));
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::appendChildByID(CDINode **child, CDI_ID id)
	{
	nerr_t err = 0;
	CDINodeData *nd = 0;
	CDINodeImpl *node = 0;
	bool nodeDataImpl = false;
	CDINode *tmp;


#ifdef SAL_DEBUG
	CDI_ID debugID;
	getNodeNameAsID(&debugID);
	if (debugID == 0)
		SAL_DebugAssertFailed("Parent node ID is Zero.  Please set parent ID.", __FILE__, __LINE__);
#endif
	if (m_flags & CDINF_ATTRIBUTE)
		{
		return (NE_CDI_INVALID_REQUEST);		
		}

	if (child != 0)
		*child = 0;
	if (id == 0)
		{
		err = NE_CDI_INVALID_PARAMETER;
		goto Exit;
		}

/*	if (m_documentData != 0)
		{
		err = m_documentData->getChildNodeDataByID(&nd, m_nodeData, id);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED) || IsBaseErr(err, NE_OBJECT_NOT_FOUND)||(nd == 0))
			err = 0;
		else
			goto Exit;
		}*/

	m_CDIDocument->lockDocument();
	if (nd == 0)
		{
		nd = (CDINodeData*)m_CDIDocument->getCDINodeDataImpl();
		if (nd == 0)
			{
			err = NE_CDI_INSUFFICIENT_MEMORY;
			goto Unlock;
			}
		nodeDataImpl = true;
		}

	err = m_CDIDocument->getCDINodeImpl(&node, this, nd);
	if (err != 0) goto Unlock;

	node->setNodeNameAsID(id);
	node->setNodeType(CDI_ELEMENT);

	if (getFirstChild(&tmp) == NE_CDI_OBJECT_NOT_FOUND)
		{
		m_firstChild = node;
		m_firstChild->setFlags(CDINF_LAST_SIBLING);
		}
	else
		appendNode(m_firstChild, node);
		
	if (child != 0)
		*child = node;
		
Unlock:
	m_CDIDocument->unlockDocument();

Exit:
	if (nd != 0)
		nd->Release();
		
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (err);
	}
//-------------------------------------------------------------------
nerr_t CDINodeImpl::appendChildByName(CDINode **child, const char *nameSpace, const char *locName)
	{
	CDI_ID id;

	id = g_cdiidalloc->getIDFromName(nameSpace, locName);
	return(appendChildByID(child, id));
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::appendChildByNameWithLen(CDINode **child, const char *nameSpace, size_t nsLen, const char *locName, size_t locLen)
	{
	CDI_ID id;

	id = g_cdiidalloc->getIDFromNameWithLen(nameSpace, nsLen, locName, locLen);
	return (appendChildByID(child, id));
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::appendChildNode(CDINode *externalElement)
	{
	nerr_t err = 0;
	//CDI_ID id;
	//CDIDocument *doc;
	CDINodeImpl *dup;
	
	if (m_flags & CDINF_ATTRIBUTE)
		{
		return (NE_CDI_INVALID_REQUEST);		
		}
		
	m_CDIDocument->lockDocument();
	err = m_CDIDocument->duplicateSubTree(&dup, (CDINodeImpl*)externalElement);
	m_CDIDocument->unlockDocument();
	if (err != 0) goto Exit;
	
	//Document data objects are read only
	//if (m_documentData != 0)
	//	{
	//	dup->getNodeNameAsID(&id);
	//	m_documentData->setExternalData(m_nodeData, CDI_RELATION_CHILD, id, dup->getNodeData());
	//	}

	m_CDIDocument->lockDocument();
	
	if (m_firstChild == 0)
		{
		m_firstChild = dup;
		m_firstChild->setFlags(CDINF_LAST_SIBLING);
		m_firstChild->setM_Parent(this);
		}
	else
		appendNode(m_firstChild, dup);

	m_CDIDocument->unlockDocument();
		
	dup->clearFlags(CDINF_DISCONNECTED);

	//doc = externalElement->getCDIDocument();
	//If the host document of the external node is not our own
	//we need to keep a reference on it
	//if (doc != this->m_CDIDocument)
	//	this->m_CDIDocument->addReferencedObject(doc);

	//doc->Release();
Exit:
	
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::appendChildWithData(CDINode **child, CDINodeData *nodeData)
	{
	nerr_t err = 0;
	CDINodeImpl *node; 
	
	*child = 0;
	
	if (m_flags & CDINF_ATTRIBUTE)
		{
		return (NE_CDI_INVALID_REQUEST);		
		}
		
	err = m_CDIDocument->getCDINodeImpl(&node, this, nodeData);
	if (err != 0)
		return (err);
	node->setNodeType(CDI_ELEMENT);
	if (m_firstChild == 0)
		m_firstChild = node;
	else
		appendNode(m_firstChild, node);

	*child = node;
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (0);
	}

//-------------------------------------------------------------------
nerr_t PCOMAPI CDINodeImpl::getAlternateNode(CDINode **altNode, CDI_ID schemaId)
	{
	nerr_t err = 0;
	CDINodeImpl *aNode = 0, *tmp = 0;
	CDINodeData *altNodeData = 0;
	CDI_ID id;
	CDIDocumentData *docData = NULL;
	
	*altNode = 0;
	if ((aNode = (CDINodeImpl*)getDynamicMember(CDIDT_ALTNODE)) != 0)
		{
		tmp = this;
		while((tmp = tmp->getAlternateNode()) != 0)
			{
			tmp->getSchemaID(&id);
			if (id == schemaId)
				{
				*altNode = tmp;
				break;
				}
			}
		}
		
	m_nodeData->getDocumentData(&docData);
	
	if ((*altNode == 0)&&(docData != 0))
		{
		m_CDIDocument->lockDocument();
		
		//have to search again for it now that I have the lock.  Someone could have put it in
		//While I wasn't looking.
		if ((aNode = (CDINodeImpl*)getDynamicMember(CDIDT_ALTNODE)) != 0)
			{
			tmp = this;
			while((tmp = tmp->getAlternateNode()) != 0)
				{
				tmp->getSchemaID(&id);
				if (id == schemaId)
					{
					*altNode = tmp;
					break;
					}
				}
			}
		
		if (*altNode != 0)
			goto Unlock;
		
		//Ask DocumentData for wanted node via getAlternateNodeDataByID()
		err = docData->getAlternateNodeDataByID(&altNodeData, this->getNodeData(), schemaId);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED) || IsBaseErr(err, NE_OBJECT_NOT_FOUND))
			{
			err = 0;
			//No alternates found
			goto Unlock;
			}
			
		if (altNodeData == 0)
			goto Unlock;
			
		err = m_CDIDocument->getCDINodeImpl((CDINodeImpl**)altNode, this, altNodeData);
		if (err != 0) goto Exit;
		
		((CDINodeImpl*)(*altNode))->setNodeType(CDI_ALTERNATE);
		err = setDynamicMember(CDIDT_ALTNODE, (void*)*altNode);

Unlock:
		m_CDIDocument->unlockDocument();
		}
		
Exit:
	if (altNodeData != 0)
		altNodeData->Release();
		
	if ((err == 0)&&(*altNode == 0))
		err = NE_CDI_OBJECT_NOT_FOUND;
		
	if (docData != NULL)
		docData->Release();
		
	return (err);
	}
//-------------------------------------------------------------------
nerr_t CDINodeImpl::setAlternateNode(CDINode **altNode, CDI_ID nodeID)
	{
#ifdef _CDI_TRACE_
	if (!m_CDIDocument->isTracing())
		gInit.traceDoc(m_CDIDocument);
#endif
	return (NE_CDI_NOT_IMPLEMENTED);
	}
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAlternateNodeList(CDINodeList **altList)
	{
	return (NE_CDI_NOT_IMPLEMENTED);
	}
//-------------------------------------------------------------------
nerr_t CDINodeImpl::deleteNode()
	{
	nerr_t err = 0;

	//m_hidden = true;
	m_flags |= CDINF_HIDDEN;

	if ((m_flags & CDINF_DISCONNECTED)&&(m_nodeData != 0))
		{
		m_nodeData->Release();
		m_nodeData = 0;
		}

#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (err);
	}
//-------------------------------------------------------------------
CDIDocument* CDINodeImpl::getCDIDocument()
	{
	m_CDIDocument->AddRef();
	return ((CDIDocument*)m_CDIDocument);
	}

//-------------------------------------------------------------------
CDIDocumentData* CDINodeImpl::getCDIDocumentData()
	{
	CDIDocumentData *docData = NULL;
	
	m_nodeData->getDocumentData(&docData);
	return (docData);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getValueAsUINT32(s_uint32 *val)
	{
	nerr_t err = 0;

	*val = 0;
	if (m_nodeData != 0)
		{
		err = m_nodeData->getValueAsUINT32(val);
		}

	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getValueAsSINT32(s_sint32 *val)
	{
	nerr_t err = 0;

	*val = 0;
	if (m_nodeData != 0)
		{
		err = m_nodeData->getValueAsSINT32(val);
		}

	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getValueAsUINT64(s_uint64 *val)
	{
	nerr_t err = 0;
		
	*val = 0;
	if (m_nodeData != 0)
		{
		err = m_nodeData->getValueAsUINT64(val);
		}

	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getValueAsSINT64(s_sint64 *val)
	{
	nerr_t err = 0;
		
	*val = 0;
	if (m_nodeData != 0)
		{
		err = m_nodeData->getValueAsSINT64(val);
		}

	return (err);
	}
	
//-------------------------------------------------------------------
void* CDINodeImpl::allocateSetBuffer(size_t size)
	{
	if (m_nodeData != 0)
		return (m_nodeData->allocateBuffer(size));
	else
		return(0);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getValueAsBinary(const void **dataPtr, size_t *bytesReturned)
	{
	nerr_t err = 0;

	*dataPtr = 0;
	*bytesReturned = 0;
	if (m_nodeData != 0)
		err = m_nodeData->getValueAsBinary(dataPtr, bytesReturned);

	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getValueAsUTF8(const s_uint8 **dataPtr, size_t *bytesReturned)
	{
	nerr_t err = 0;
	const s_uint16* uniData;
	size_t dataSize;
	void *tmp;


	*dataPtr = 0;
	*bytesReturned = 0;
	if (m_nodeData != 0)
		{
		err = m_nodeData->getValueAsUTF8(dataPtr, bytesReturned);
		if ((*dataPtr == 0)&&(m_nodeData->getValueType() == CDIT_UCS2)&&(err != 0))
			{
			if ((tmp = clearDynamicMember(CDIDT_ALTDATABUF)) != 0)
				gInit.free(tmp);

			err = m_nodeData->getValueAsUCS2(&uniData, &dataSize);
			if (err == 0)
				{
									//double the size of the buffer just in case
				tmp = gInit.malloc(dataSize * 2);
				if (tmp == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				if (uniToUtf8((s_uint8*)tmp, uniData, &dataSize))
					{
					err = NE_CDI_DATA_CONVERSION_ERR;
					goto Exit;
					}
				*dataPtr = (s_uint8*)tmp;
				*bytesReturned = dataSize;
				err = setDynamicMember(CDIDT_ALTDATABUF, tmp);
				}
			}
		else if ((*dataPtr == 0)&&(m_nodeData->getValueType() == CDIT_ID)&&(err != 0))
			{
			err = getValueAsString((const char**)dataPtr, bytesReturned);
			}
		}

Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getValueAsUCS2(const s_uint16 **dataPtr, size_t *unisReturned)
	{
	nerr_t err = 0;
	const s_uint8 *utf8Data;
	size_t dataSize;
	void *tmp;


	*dataPtr = 0;
	*unisReturned = 0;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->getValueAsUCS2(dataPtr, unisReturned);
		if ((*dataPtr == 0)&&(m_nodeData->getValueType() == CDIT_UTF8)&&(err != 0))
			{
			if ((tmp = clearDynamicMember(CDIDT_ALTDATABUF)) != 0)
				gInit.free(tmp);

			err = m_nodeData->getValueAsUTF8(&utf8Data, &dataSize);
			if (err == 0)
				{
				tmp = gInit.malloc(dataSize * sizeof(s_uint16));
				if (tmp == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				if (utf8ToUni((s_uint16*)tmp, utf8Data, &dataSize) != 0)
					{
					err = NE_CDI_DATA_CONVERSION_ERR;
					goto Exit;
					}
				*dataPtr = (s_uint16*)tmp;
				*unisReturned = dataSize;
				err = setDynamicMember(CDIDT_ALTDATABUF, tmp);
				}
			}
		}

Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getValueAsString(const char **dataPtr, size_t *bytesReturned)
	{
	nerr_t err = 0;
	CDI_VALUE_TYPE type;
	s_uint32 data32; 
	s_sint32 sdata32;
	s_uint64 data64;
	s_sint64 sdata64;
	const s_uint8 *data8;
	const s_uint16 *uniData;
	size_t dataSize, buffSize;
	CDI_ID id;
	size_t nsLen, locLen, tagLen;
	const char *ns, *loc, *tag;
	void *tmp = 0;


	*dataPtr = 0;
	*bytesReturned = 0;
	if (m_nodeData != 0)
		{
		type = m_nodeData->getValueType();

		//Calls getValueAsString first to see if it is implemented
//		err = m_nodeData->getValueAsString(dataPtr, bytesReturned);
//		if (err == 0)
//			goto Exit;

		if ((type != CDIT_STRING)&&(type != CDIT_UTF8))
			{
			if ((tmp = clearDynamicMember(CDIDT_ALTDATABUF)) != 0)
				gInit.free(tmp);
			}

		switch (type)
			{
			case CDIT_STRING:
				err = m_nodeData->getValueAsString(dataPtr, bytesReturned);
				break;
			case CDIT_UTF8:
				err = m_nodeData->getValueAsUTF8((const s_uint8**)dataPtr, bytesReturned);
				if (err == 0)
					{
					s_uint32 escapeCount = countNeededEscapeChars((const char*)*dataPtr, *bytesReturned);
					if (escapeCount > 0)
						{
						buffSize = *bytesReturned + (escapeCount * 6);
						tmp = gInit.malloc(buffSize);
						if (tmp == 0)
							{
							err = NE_CDI_INSUFFICIENT_MEMORY;
							goto Exit;
							}
						err = escapeBuffer((char*)tmp, (const char*)*dataPtr, bytesReturned);
						s_assert(*bytesReturned <= buffSize);
						*dataPtr = (const char*)tmp;
						}
					}
				break;
			case CDIT_UINT32:
				tmp = gInit.malloc(ALT_DATA_SIZE);
				if (tmp == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				err = m_nodeData->getValueAsUINT32(&data32);
				*dataPtr = u64toa((char*)tmp, ALT_DATA_SIZE, (s_uint64)data32);
				*bytesReturned = strlen(*dataPtr);
				s_assert(*bytesReturned <= ALT_DATA_SIZE);
				break;
			case CDIT_SINT32:
				tmp = gInit.malloc(ALT_DATA_SIZE);
				if (tmp == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				err = m_nodeData->getValueAsSINT32(&sdata32);
				sprintf((char*)tmp, "%d", sdata32);
				*dataPtr = (const char*)tmp;
				*bytesReturned = strlen(*dataPtr);
				s_assert(*bytesReturned <= ALT_DATA_SIZE);
				break;
			case CDIT_UINT64:
				tmp = gInit.malloc(ALT_DATA_SIZE);
				if (tmp == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				err = m_nodeData->getValueAsUINT64(&data64);
				*dataPtr = u64toa((char*)tmp, ALT_DATA_SIZE, data64);
				*bytesReturned = strlen(*dataPtr);
				s_assert(*bytesReturned <= ALT_DATA_SIZE);
				break;
			case CDIT_SINT64:
				tmp = gInit.malloc(ALT_DATA_SIZE);
				if (tmp == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				err = m_nodeData->getValueAsSINT64(&sdata64);
				*dataPtr = s64toa((char*)tmp, ALT_DATA_SIZE, sdata64);
				*bytesReturned = strlen(*dataPtr);
				s_assert(*bytesReturned <= ALT_DATA_SIZE);
				break;
			case CDIT_UCS2:
				err = m_nodeData->getSizeAsUCS2(&dataSize);
								//dataSize is in UCS2 size  times 2 to get bytes
				buffSize = dataSize * 4;	//Utf8 should only get to be 1.5 times bigger.  double buffer to be safe.
				tmp = gInit.malloc(buffSize);
				if (tmp == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				err = m_nodeData->getValueAsUCS2(&uniData, &dataSize);
				if (err != 0)
					goto Exit;
				if (uniToUtf8((s_uint8*)tmp, uniData, &dataSize))
					{
					err = NE_CDI_DATA_CONVERSION_ERR;
					goto Exit;
					}
				s_assert(dataSize <= buffSize);
				*bytesReturned = dataSize;
				*dataPtr = (const char*)tmp;
				break;
			case CDIT_BINARY:
				err = m_nodeData->getSizeAsBinary(&dataSize);
				buffSize = dataSize * 2 + 2;  //double the buffer size
				tmp = gInit.malloc(buffSize);
				if (tmp == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				err = m_nodeData->getValueAsBinary((const void**)&data8, &dataSize);
				if (err != 0)
					goto Exit;
				if (b64encode((char*)tmp, (const char*)data8, &dataSize) != 0)
					{
					err = NE_CDI_DATA_CONVERSION_ERR;
					goto Exit;
					}
				s_assert(dataSize <= buffSize);
				*bytesReturned = dataSize;
				*dataPtr = (const char *)tmp;
				break;
			case CDIT_ID:
				err = m_nodeData->getValueAsID(&id);
				if (err != 0) goto Exit;
				
				g_cdiidalloc->getNameFromIDAsPtr(id, &ns, &nsLen, &loc, &locLen);
				m_CDIDocument->getTagFromNameSpace((CDINode*)this, &tag, &tagLen, ns, nsLen);
				dataSize = tagLen + locLen + 2;  //Add extra for ':' and a null at the end
				tmp = gInit.malloc(dataSize);
				if (tmp == 0)
					{
					err = NE_CDI_INSUFFICIENT_MEMORY;
					goto Exit;
					}
				else
					{
					if (tagLen != 0)
						{
						strncpy((char*)tmp, tag, tagLen);
						((char*)tmp)[tagLen] = ':';
						((char*)tmp)[tagLen+1] = '\0';
						strncat((char*)tmp, loc, locLen);
						}
					else
						{
						strncpy((char*)tmp, loc, locLen);
						dataSize = locLen + 1;
						}
					}
				*bytesReturned = (dataSize-1);
				*dataPtr = (const char*)tmp;
				break;
			default:
				break;
			}		
		}

	if ((tmp != 0)&&(err == 0))
		{
		void *tmpPtr;
		if ((tmpPtr = clearDynamicMember(CDIDT_ALTDATABUF)) != 0)
			gInit.free(tmpPtr);
		
		err = setDynamicMember(CDIDT_ALTDATABUF, tmp);
		}

Exit:
	return (err);
	}

//-------------------------------------------------------------------
s_uint32 CDINodeImpl::countNeededEscapeChars(const char *buffer, size_t size)
	{
	s_uint32 count = 0;
	
	for (size_t I = 0; I < size; I ++)
		{
		if ((buffer[I] == '<') || (buffer[I] == '>') ||
				(buffer[I] == '"') || (buffer[I] == '\'') ||
				(buffer[I] == '&'))
			{
			//_asm{int 3};
			count ++;
			}
		}
	return (count);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::escapeBuffer(char *outbuf, const char *inbuf, size_t *bufsize)
	{
	char *dest = outbuf;
	const char *src = inbuf;
	
	while (src < (inbuf + *bufsize))
		{
		switch (*src)
			{
			case '&':
				strncpy(dest, "&amp;", 5);
				dest += 5;
				break;
			case '<':
				strncpy(dest, "&lt;", 4);
				dest += 4;
				break;
			case '>':
				strncpy(dest, "&gt;", 4);
				dest += 4;
				break;
			case '\'':
				strncpy(dest, "&apos;", 6);
				dest += 6;
				break;
			case '"':
				strncpy(dest, "&quot;", 6);
				dest += 6;
				break;
			default :
				*dest = *src;
				dest++;
			}
		src++;
		}
		
	*bufsize = dest - outbuf;
	
	return (0);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::checkDuplicateData()
	{
	nerr_t err = 0;
	
	if (m_flags & CDINF_SHARED_DATA)
		{
		err = getNewNodeData();
		}
		
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getNewNodeData()
	{
	nerr_t err = 0;
	CDI_ID id;
	
	getNodeNameAsID(&id); //Ensures the name is set before we swap out the nodeData object
	if (m_nodeData != 0)
		m_nodeData->Release();
	
	m_nodeData = m_CDIDocument->getCDINodeDataImpl();
	if (m_nodeData == 0)
		err = NE_CDI_INSUFFICIENT_MEMORY;

	return (err);
	}
	
//-------------------------------------------------------------------
//This is called before a new node data is set
//The first node of each branch must be checked for before swapping out the node data
//Because the nodeData may contain state info for the documentData object.
void CDINodeImpl::checkForBranches()
	{
	CDINode *n;
	
	getFirstChild(&n);
	getFirstAttribute(&n);
	getNextSibling(&n);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::setValueAsUINT32(s_uint32 value)
	{
	nerr_t err = 0;

	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->setValueAsUINT32(value);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->setValueAsUINT32(value);
			}
		}
	else
		err = NE_CDI_MISSING_NODE_DATA;
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}
//-------------------------------------------------------------------
nerr_t CDINodeImpl::setValueAsSINT32(s_sint32 value)
	{
	nerr_t err = 0;

	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->setValueAsSINT32(value);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->setValueAsSINT32(value);
			}
		}
	else
		err = NE_CDI_MISSING_NODE_DATA;
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::setValueAsUINT64(s_uint64 value)
	{
	nerr_t err = 0;

	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->setValueAsUINT64(value);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->setValueAsUINT64(value);
			}
		}
	else
		err = NE_CDI_MISSING_NODE_DATA;
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::setValueAsSINT64(s_sint64 value)
	{
	nerr_t err = 0;

	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->setValueAsSINT64(value);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->setValueAsSINT64(value);
			}
		}
	else
		err = NE_CDI_MISSING_NODE_DATA;
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::setValueAsBinary(void *dataPtr, size_t size)
	{
	nerr_t err = 0;

	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->setValueAsBinary(dataPtr, size);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->setValueAsBinary(dataPtr, size);
			}
		}
	else
		err = NE_CDI_MISSING_NODE_DATA;
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::setValueAsUTF8(s_uint8 *dataPtr, size_t size)
	{
	nerr_t err = 0;

	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->setValueAsUTF8(dataPtr, size);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->setValueAsUTF8(dataPtr, size);
			}
		}
	else
		err = NE_CDI_MISSING_NODE_DATA;
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::setValueAsUCS2(s_uint16 *uniPtr, size_t uniSize)
	{
	nerr_t err = 0;

	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->setValueAsUCS2(uniPtr, uniSize);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->setValueAsUCS2(uniPtr, uniSize);
			}
		}
	else
		err = NE_CDI_MISSING_NODE_DATA;
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::setValueAsString(char *chPtr, size_t size)
	{
	nerr_t err = 0;

	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->setValueAsString(chPtr, size);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->setValueAsString(chPtr, size);
			}
		}
	else
		err = NE_CDI_MISSING_NODE_DATA;
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::readValueAsBinary(s_uint64 *pos, void *bufPtr, size_t *readSz)
	{
	nerr_t err = 0;
	if (m_nodeData != 0)
		err = m_nodeData->readValueAsBinary(pos, bufPtr, readSz);
	
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getSizeAsBinary(size_t *size)
	{
	nerr_t err = 0;
	if (m_nodeData != 0)
		err = m_nodeData->getSizeAsBinary(size);
	
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::readValueAsUTF8(s_uint64 *pos, s_uint8 *bufPtr, size_t *readSz)
	{
	nerr_t err = 0;
	if (m_nodeData != 0)
		err = m_nodeData->readValueAsUTF8(pos, bufPtr, readSz);
	
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getSizeAsUTF8(size_t *size)
	{
	nerr_t err = 0;
	if (m_nodeData != 0)
		err = m_nodeData->getSizeAsUTF8(size);
	
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::readValueAsUCS2(s_uint64 *pos, s_uint16 *bufPtr, size_t *readSz)
	{
	nerr_t err = 0;
	if (m_nodeData != 0)
		err = m_nodeData->readValueAsUCS2(pos, bufPtr, readSz);
	
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getSizeAsUCS2(size_t *uniSize)
	{
	nerr_t err = 0;
	if (m_nodeData != 0)
		err = m_nodeData->getSizeAsUCS2(uniSize);
	
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::readValueAsString(s_uint64 *pos, char *bufPtr, size_t *readSz)
	{
	nerr_t err = 0;
	if (m_nodeData != 0)
		err = m_nodeData->readValueAsString(pos, bufPtr, readSz);
	
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getSizeAsString(size_t *size)
	{
	nerr_t err = 0;
	if (m_nodeData != 0)
		err = m_nodeData->getSizeAsString(size);
	
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::writeValueAsBinary(s_uint64 *pos, s_uint8 *bufPtr, size_t writeSz)
	{
	nerr_t err = 0;
	
	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->writeValueAsBinary(pos, bufPtr, writeSz);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->writeValueAsBinary(pos, bufPtr, writeSz);
			}
		}
	
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::writeValueAsUTF8(s_uint64 *pos, s_uint8 *bufPtr, size_t writeSz)
	{
	nerr_t err = 0;
	
	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->writeValueAsUTF8(pos, bufPtr, writeSz);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->writeValueAsUTF8(pos, bufPtr, writeSz);
			}
		}
	
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::writeValueAsUCS2(s_uint64 *pos, s_uint16 *uniPtr, size_t writeSz)
	{
	nerr_t err = 0;
	
	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->writeValueAsUCS2(pos, uniPtr, writeSz);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->writeValueAsUCS2(pos, uniPtr, writeSz);
			}
		}
	
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::writeValueAsString(s_uint64 *pos, char *chPtr, size_t writeSz)
	{
	nerr_t err = 0;
	
	if ((err = checkDuplicateData()) != 0) goto Exit;
	
	if (m_nodeData != 0)
		{
		err = m_nodeData->writeValueAsString(pos, chPtr, writeSz);
		if (IsBaseErr(err, NE_NOT_IMPLEMENTED))
			{
			checkForBranches();
			err = getNewNodeData();
			if (err == 0)
				err = m_nodeData->writeValueAsString(pos, chPtr, writeSz);
			}
		}
	
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
Exit:
	return (err);
	}

//-------------------------------------------------------------------
void CDINodeImpl::completed(void)
	{
	if (m_nodeData != 0)
		m_nodeData->completed();
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::useValueAs(PCIUnknown **obj, RPCCLSID rclsid)
	{
	nerr_t err = 0;
	CDIValueInterpreter *vi = 0;

#ifndef _CDI_TEST_
	err = PsaComCreateInstance(rclsid, 0, IID_CDIValueInterpreter, (void**)&vi);
#endif
	if (err == 0)
		{
		//Implementer of vi object needs to call AddRef() on m_nodeData
		vi->setNode(this);
		}

	*obj = vi;

	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::createDocumentFromNodeWN(CDIDocument **newDoc)
	{
	return (createDocumentFromNode(newDoc, false));
	}
//-------------------------------------------------------------------
nerr_t PCOMAPI CDINodeImpl::createDocumentFromNode(CDIDocument **newDoc)
	{
	return (createDocumentFromNode(newDoc, true));
	}
//-------------------------------------------------------------------
nerr_t CDINodeImpl::createDocumentFromNode(CDIDocument **newDoc, bool inherit)
	{
	nerr_t err = 0;
	CDINodeImpl *dup;
	CDIDocumentData *docData;
	
	//_asm{int 3};
	/*if (m_parent == 0)
		{
		//This is the root node so addref the CDIDoc and return it
		*newDoc = m_CDIDocument;
		(*newDoc)->AddRef();
		}
	else
		{*/
		CDIDocumentImpl *doc;
		err = m_CDIDocument->getCDIDocumentImpl(&doc);
		if (err != 0) goto Exit;
		
		doc->lockDocument();
		doc->duplicateSubTree(&dup, this);
		doc->unlockDocument();
		dup->setM_NextSibling(0);
		dup->setM_PrevSibling(0);
		dup->setM_Parent(0);
		
		if (inherit)
			inheritNamespaces(dup);
		
		doc->setRootElement(dup);
		
		docData = m_CDIDocument->getDocumentData();
		if (docData != 0)
			{
			doc->setM_DocumentData(docData);
			docData->Release();
			}
			
		
		doc->setHostDocument(m_CDIDocument);
		*newDoc = (CDIDocument*)doc;
		//}
	
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getNodeNamespaceAsPtr(const char **nsPtr, size_t *nsLen)
	{
	nerr_t err = 0;
	size_t llen;
	const char *l;

	*nsPtr = 0;
	*nsLen = 0;

	if ((m_nodeID == 0)&&(m_nodeData != 0))
		{
		m_nodeID = m_nodeData->getNodeID();
#ifdef _DEBUG
		size_t lentemp;
		const char *nstemp;
		
		g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &nstemp, &lentemp, &m_nodeName, &lentemp);
#endif
		}

	g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, nsPtr, nsLen, &l, &llen);

	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getNodeLocalNameAsPtr(const char **lPtr, size_t *locLen)
	{
	nerr_t err = 0;
	size_t nslen;
	const char *ns;

	*lPtr = 0;
	*locLen = 0;

	if ((m_nodeID == 0)&&(m_nodeData != 0))
		{
		m_nodeID = m_nodeData->getNodeID();
#ifdef _DEBUG
		size_t lentemp;
		const char *nstemp;
		
		g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &nstemp, &lentemp, &m_nodeName, &lentemp);
#endif
		}

	g_cdiidalloc->getNameFromIDAsPtr(m_nodeID, &ns, &nslen, lPtr, locLen);

	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::insertBefore(CDINode *externalNode)
	{
	nerr_t err = 0;
	CDINodeImpl *newNode;
	//CDIDocument *doc;

	//_asm{int 3};
	m_CDIDocument->lockDocument();
	err = m_CDIDocument->duplicateSubTree(&newNode, (CDINodeImpl*)externalNode);
	m_CDIDocument->unlockDocument();
	if (err != 0) goto Exit;
	
	if (m_parent == 0)
		{
		err = NE_CDI_INSERT_MISSING_PARENT;
		goto Exit;
		}

	m_CDIDocument->lockDocument();
	
	newNode->m_parent = m_parent;
	newNode->m_nextSibling = this;
	if (m_prevSibling == 0)
		m_parent->m_firstChild = newNode;
	else
		{
		newNode->m_prevSibling = m_prevSibling;
		m_prevSibling->m_nextSibling = newNode;
		}

	m_prevSibling = newNode;
	
	//doc = externalNode->getCDIDocument();
	//if (doc != this->m_CDIDocument)
	//	this->m_CDIDocument->addReferencedObject(doc);
	
	m_CDIDocument->unlockDocument();
	
	//doc->Release();

#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif

Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::insertDocBefore(CDIDocument *doc)
	{
	nerr_t err = 0;
	CDINode *n;

	err = doc->getRootElement(&n);
	if (err == 0)
		err = insertBefore(n);
	
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getSchemaID(CDI_ID *id)
	{
	nerr_t err = 0;
	CDINode *att = 0;
	
	*id = 0;
	
	err = getAttribute(&att, gInit.getTag(XSI_TYPE));	
	if (err == 0)
		{
		att->getValueAsID(id);
		goto Exit;
		}
	else
		err = 0;
	
	if (m_nodeData != 0)
		err = m_nodeData->getSchemaID(id);
	
	if (*id != 0)
		setSchemaID(*id);  //this adds the attribute to the node
Exit:
	return (err);
	}
//-------------------------------------------------------------------
nerr_t CDINodeImpl::setSchemaID(CDI_ID id)
	{
	nerr_t err = 0;
	CDINode *att = 0;
	
	err = setAttribute(&att, gInit.getTag(XSI_TYPE));
	if (err != 0) goto Exit;
	
	err = att->setValueAsID(id);
	
Exit:
	return (err);
	}


//-------------------------------------------------------------------
nerr_t CDINodeImpl::insertNodeByID(CDINode **newSib, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDINodeData *nd = 0;
	CDINodeImpl *node = 0;
	
	if (newSib != 0)
		*newSib = 0;
		
	if (elementId == 0)
		{
		err = NE_CDI_INVALID_PARAMETER;
		goto Exit;
		}
	
	m_CDIDocument->lockDocument();
	nd = (CDINodeData*)m_CDIDocument->getCDINodeDataImpl();
	if (nd == 0)
		{
		err = NE_CDI_INSUFFICIENT_MEMORY;
		goto Unlock;
		}
		
	nd->setNodeID(elementId);
	err = m_CDIDocument->getCDINodeImpl(&node, this, nd);
	if (err != 0) goto Unlock;
	
	if (m_prevSibling == 0)
		m_parent->m_firstChild = node;
	else
		{
		node->m_prevSibling = m_prevSibling;
		m_prevSibling->m_nextSibling = node;
		}
	m_prevSibling = node;
	node->m_parent = m_parent;
	node->m_nextSibling = this;
	
	if (newSib != 0)
		*newSib = node;
	
Unlock:
	m_CDIDocument->unlockDocument();
	
Exit:
	if (nd != 0)
		nd->Release();
		
#ifdef _CDI_TRACE_
	if ((!m_CDIDocument->isTracing())&&(err == 0))
		gInit.traceDoc(m_CDIDocument);
#endif
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getChildElement(CDINode **node, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDI_ID id;
	
	err = getFirstChild(node);
	if (err != 0) goto Exit;
	(*node)->getNodeNameAsID(&id);
	if (id == elementId) goto Exit;
	
	err = (*node)->getSiblingByID(node, elementId);
/*
	CDINodeList *list;
	
	err = getElementsByID(&list, elementId);
	if (err != 0)
		goto Exit;
		
	err = list->getFirstNode(node);	
	*/
Exit:
	if (err != 0)
		*node = NULL;
		
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getChildElementValueAsBinary(const void **dataPtr, size_t *bytesReturned, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getChildElement(&node, elementId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsBinary(dataPtr, bytesReturned);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getChildElementValueAsUTF8(const s_uint8 **dataPtr, size_t *bytesReturned, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getChildElement(&node, elementId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsUTF8(dataPtr, bytesReturned);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getChildElementValueAsUCS2(const s_uint16 **dataPtr, size_t *unisReturned, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getChildElement(&node, elementId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsUCS2(dataPtr, unisReturned);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getChildElementValueAsString(const char **dataPtr, size_t *bytesReturned, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getChildElement(&node, elementId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsString(dataPtr, bytesReturned);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getChildElementValueAsSINT32(s_sint32 *val, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getChildElement(&node, elementId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsSINT32(val);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getChildElementValueAsUINT32(s_uint32 *val, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getChildElement(&node, elementId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsUINT32(val);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getChildElementValueAsUINT64(s_uint64 *val, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getChildElement(&node, elementId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsUINT64(val);
Exit:
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getChildElementValueAsSINT64(s_sint64 *val, CDI_ID elementId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getChildElement(&node, elementId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsSINT64(val);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttributeValueAsBinary(const void **dataPtr, size_t *bytesReturned, CDI_ID attributeId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getAttribute(&node, attributeId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsBinary(dataPtr, bytesReturned);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttributeValueAsUTF8(const s_uint8 **dataPtr, size_t *bytesReturned, CDI_ID attributeId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getAttribute(&node, attributeId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsUTF8(dataPtr, bytesReturned);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttributeValueAsUCS2(const s_uint16 **dataPtr, size_t *unisReturned, CDI_ID attributeId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getAttribute(&node, attributeId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsUCS2(dataPtr, unisReturned);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttributeValueAsString(const char **dataPtr, size_t *bytesReturned, CDI_ID attributeId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getAttribute(&node, attributeId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsString(dataPtr, bytesReturned);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttributeValueAsSINT32(s_sint32 *val, CDI_ID attributeId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getAttribute(&node, attributeId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsSINT32(val);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttributeValueAsUINT32(s_uint32 *val, CDI_ID attributeId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getAttribute(&node, attributeId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsUINT32(val);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttributeValueAsUINT64(s_uint64 *val, CDI_ID attributeId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getAttribute(&node, attributeId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsUINT64(val);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::getAttributeValueAsSINT64(s_sint64 *val, CDI_ID attributeId)
	{
	nerr_t err = 0;
	CDINode *node;
	
	err = getAttribute(&node, attributeId);
	if (err != 0) goto Exit;
	
	err = node->getValueAsSINT64(val);
Exit:
	return (err);
	}
	
//-------------------------------------------------------------------
nerr_t CDINodeImpl::serialize(CDIWrite *wrt, s_uint32 flags)
	{
	nerr_t err = 0;
	
	err = m_CDIDocument->serializeNode(this, wrt, flags);
	
	return (err);
	}

//-------------------------------------------------------------------
nerr_t CDINodeImpl::getSiblingByID(CDINode **sibling, CDI_ID id)
	{
	nerr_t err = 0;
	CDI_ID sibId;
	
	if (id == 0)
		{
		err = NE_CDI_INVALID_PARAMETER;
		goto Exit;
		}
		
	*sibling = (CDINode*)this;
	while ((err = (*sibling)->getNextSibling(sibling)) == 0)
		{
		(*sibling)->getNodeNameAsID(&sibId);
		if (id == sibId)
			break;
		}	
	
Exit:
	if (err != 0)
		*sibling = 0;
		
	return (err);
	}

//-------------------------------------------------------------------
/*public*/
void CDINodeImpl::setNodeFlags(s_uint32 flags)
	{
	setFlags(flags);
	}


//- End of file -


